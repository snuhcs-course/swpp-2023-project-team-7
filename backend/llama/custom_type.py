import tiktoken
import sys
from tenacity import (
    retry,
	stop_after_attempt,
	wait_random_exponential,
)  # for exponential backoff
import openai
import pickle
import torch

from threading import Thread
from transformers import AutoModelForCausalLM, AutoTokenizer, TextIteratorStreamer

from llama.constants import (
    GPT_SYSTEM_SUMMARY_PROMPT_FROM_INTERMEDIATE, 
	GPT_SYSTEM_SUMMARY_PROMPT_FROM_RAW,
	GPT_SYSTEM_QUIZ_PROMPT_FROM_INTERMEDIATE,
	GPT_SYSTEM_QUIZ_PROMPT_FROM_RAW,
	GPT_TEXT_TO_INTERMEDIATE_SYSTEM_PROMPT,
	GPT_INTERMEDIATE_TO_INTERMEDAITE_SYSTEM_SUMMARY_PROMPT,
	GPT_INTERMEDAITE_TO_FINAL_SYSTEM_SUMMARY_PROMPT,
	PROMPT_TEMPLATE_INTERMEDIATE_FRONT,
	PROMPT_TEMPLATE_INTERMEDIATE_BACK,
	PROMPT_TEMPLATE_FINAL_FRONT,
	PROMPT_TEMPLATE_FINAL_BACK,
)

class Summary:
	def __init__(
		self,
		parent=None,
		start_idx=0,
		end_idx=0,
		summary_content=None,
		children=[]
	):
		# parent -> reduced summary
		self.parent = parent
		# child -> unreduced summary
		self.children = children

		self.start_idx = start_idx
		self.end_idx = end_idx
		self.summary_content = summary_content
	
	def find_leaf_summary(self, word_index):
		if len(self.children) == 0:
			return self
		for child in self.children:
			if word_index >= child.start_idx and word_index <= child.end_idx:
				return child.find_leaf_summary(word_index)

	def find_included_summaries(self, child_summary):
		included_summaries = []
		while child_summary.parent is not None:
			siblings = [child for child in child_summary.parent.children if child.end_idx <= child_summary.start_idx]
			included_summaries.extend(siblings)
			child_summary = child_summary.parent

		return included_summaries
	
	def __eq__(self, other):
		if isinstance(other, Summary):
			return self.summary_content == other.summary_content and self.start_idx == other.start_idx and self.end_idx == other.end_idx
		return False

	def __str__(self):
		return f"Summary(start_idx={self.start_idx}, end_idx={self.end_idx}, summary_content={self.summary_content})"
	
	def __hash__(self):
		return hash((self.start_idx, self.end_idx, self.summary_content))


class AIBackend:
	def get_summary_from_text(self, progress, book_content_url):
		pass

	def get_summary_from_intermediate(self, progress, book_content_url, summary_tree_url):
		pass

	def get_quiz_from_text(self, progress, book_content_url):
		pass

	def get_quiz_from_intermediate(self, progress, book_content_url, summary_tree_url):
		pass

	def precompute_intermediate_from_text(self, sliced_text):
		pass

	def precompute_intermediate_from_intermediate(self, content):
		pass

	def precompute_final_from_intermediate(self, content):
		pass

class ProxyAIBackend(AIBackend):
	def __init__(self, summary_generator):
		self.summary_generator = summary_generator

	def precompute_intermediate_from_text(self, sliced_text):
		return self.summary_generator.precompute_intermediate_from_text(sliced_text)

	def precompute_intermediate_from_intermediate(self, content):
		return self.summary_generator.precompute_intermediate_from_intermediate(content)
	
	def precompute_final_from_intermediate(self, content):
		return self.summary_generator.precompute_final_from_intermediate(content)

	def get_summary_from_text(self, progress, book_content_url):
		return self.summary_generator.get_summary_from_text(progress, book_content_url)

	def get_summary_from_intermediate(self, progress, book_content_url, summary_tree_url):
		return self.summary_generator.get_summary_from_intermediate(progress, book_content_url, summary_tree_url)

	def get_quiz_from_text(self, progress, book_content_url):
		return self.summary_generator.get_quiz_from_text(progress, book_content_url)

	def get_quiz_from_intermediate(self, progress, book_content_url, summary_tree_url):
		return self.summary_generator.get_quiz_from_intermediate(progress, book_content_url, summary_tree_url)

class GPT4Backend(AIBackend):
	def __init__(self):
		self.tokenizer = tiktoken.get_encoding("cl100k_base")

	@retry(wait=wait_random_exponential(multiplier=1, max=60), stop=stop_after_attempt(6))
	def completion_with_backoff(self, **kwargs):
		return openai.ChatCompletion.create(**kwargs)

	def get_summary_from_text(self, progress, book_content_url):
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_SYSTEM_SUMMARY_PROMPT_FROM_RAW},
				{"role": "user", "content": read_content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def get_summary_from_intermediate(self, progress, book_content_url, summary_tree_url):
		"""
		generates summary based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		:param callback: callback function to call when a delta content is generated
		"""

		summary_tree = ""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		with open(summary_tree_url, 'rb') as pickle_file:
			summary_tree = pickle.load(pickle_file)

		# word_index -> the number of characters read by the user.
		# start_index, end_idx is the number of tokens processed by the summary
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]
		tokenized_read_content = self.tokenizer.encode(read_content)
		word_index = len(tokenized_read_content) - 1

		leaf = summary_tree.find_leaf_summary(word_index=word_index)
		available_summary_list = summary_tree.find_included_summaries(leaf)

		content = "\n\n".join([summary.summary_content for summary in available_summary_list])
		content += "\n\n" + book_content[leaf.start_idx:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_SYSTEM_SUMMARY_PROMPT_FROM_INTERMEDIATE},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished
			if finished:
				break

	def get_quiz_from_text(self, progress, book_content_url):
		"""
		generates 10 quizzes based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		"""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()

		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_SYSTEM_QUIZ_PROMPT_FROM_RAW},
				{"role": "user", "content": read_content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def get_quiz_from_intermediate(self, progress, book_content_url, summary_tree_url):
		"""
		generates 10 quizzes based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		"""
		summary_tree = ""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		with open(summary_tree_url, 'rb') as pickle_file:
			summary_tree = pickle.load(pickle_file)

		# word_index -> the number of characters read by the user.
		# start_index, end_idx is the number of tokens processed by the summary
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]
		tokenized_read_content = self.tokenizer.encode(read_content)
		word_index = len(tokenized_read_content)-1
		
		# generate new quiz
		leaf = summary_tree.find_leaf_summary(word_index=word_index)
		available_summary_list = summary_tree.find_included_summaries(leaf)

		content = "\n\n".join([summary.summary_content for summary in available_summary_list])
		content += "\n\n" + book_content[leaf.end_idx:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_SYSTEM_QUIZ_PROMPT_FROM_INTERMEDIATE},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break
			
	def precompute_intermediate_from_text(self, sliced_text):
		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_TEXT_TO_INTERMEDIATE_SYSTEM_PROMPT},
				{"role": "user", "content": sliced_text}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished
			if finished:
				break

	def precompute_intermediate_from_intermediate(self, content):
		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_INTERMEDIATE_TO_INTERMEDAITE_SYSTEM_SUMMARY_PROMPT},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content

			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def precompute_final_from_intermediate(self, content):
		for resp in self.completion_with_backoff(
			model="gpt-4", messages=[
				{"role": "system", "content": GPT_INTERMEDAITE_TO_FINAL_SYSTEM_SUMMARY_PROMPT},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content

			sys.stdout.write(delta_content)
			sys.stdout.flush()
			yield delta_content, finished

			if finished:
				break


class GPT3Backend(AIBackend):
	def __init__(self):
		self.tokenizer = tiktoken.get_encoding("cl100k_base")

	@retry(wait=wait_random_exponential(multiplier=1, max=60), stop=stop_after_attempt(6))
	def completion_with_backoff(self, **kwargs):
		return openai.ChatCompletion.create(**kwargs)

	def get_summary_from_text(self, progress, book_content_url):
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_SYSTEM_SUMMARY_PROMPT_FROM_RAW},
				{"role": "user", "content": read_content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def get_summary_from_intermediate(self, progress, book_content_url, summary_tree_url):
		"""
		generates summary based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		:param callback: callback function to call when a delta content is generated
		"""

		summary_tree = ""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		with open(summary_tree_url, 'rb') as pickle_file:
			summary_tree = pickle.load(pickle_file)

		# word_index -> the number of characters read by the user.
		# start_index, end_idx is the number of tokens processed by the summary
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]
		tokenized_read_content = self.tokenizer.encode(read_content)
		word_index = len(tokenized_read_content) - 1

		leaf = summary_tree.find_leaf_summary(word_index=word_index)
		available_summary_list = summary_tree.find_included_summaries(leaf)

		content = "\n\n".join([summary.summary_content for summary in available_summary_list])
		content += "\n\n" + book_content[leaf.start_idx:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_SYSTEM_SUMMARY_PROMPT_FROM_INTERMEDIATE},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished
			if finished:
				break

	def get_quiz_from_text(self, progress, book_content_url):
		"""
		generates 10 quizzes based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		"""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()

		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_SYSTEM_QUIZ_PROMPT_FROM_RAW},
				{"role": "user", "content": read_content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def get_quiz_from_intermediate(self, progress, book_content_url, summary_tree_url):
		"""
		generates 10 quizzes based on the word_index
		:param progress: progress of the book
		:param book_id: book id to generate quiz from
		"""
		summary_tree = ""
		with open(book_content_url, 'r') as book_file:
			book_content = book_file.read()
		with open(summary_tree_url, 'rb') as pickle_file:
			summary_tree = pickle.load(pickle_file)

		# word_index -> the number of characters read by the user.
		# start_index, end_idx is the number of tokens processed by the summary
		word_index = int(progress * len(book_content))
		read_content = book_content[:word_index]
		tokenized_read_content = self.tokenizer.encode(read_content)
		word_index = len(tokenized_read_content)-1
		
		# generate new quiz
		leaf = summary_tree.find_leaf_summary(word_index=word_index)
		available_summary_list = summary_tree.find_included_summaries(leaf)

		content = "\n\n".join([summary.summary_content for summary in available_summary_list])
		content += "\n\n" + book_content[leaf.end_idx:word_index]

		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_SYSTEM_QUIZ_PROMPT_FROM_INTERMEDIATE},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break
			
	def precompute_intermediate_from_text(self, sliced_text):
		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_TEXT_TO_INTERMEDIATE_SYSTEM_PROMPT},
				{"role": "user", "content": sliced_text}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content
			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished
			if finished:
				break

	def precompute_intermediate_from_intermediate(self, content):
		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_INTERMEDIATE_TO_INTERMEDAITE_SYSTEM_SUMMARY_PROMPT},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content

			sys.stdout.write(delta_content)
			sys.stdout.flush()

			yield delta_content, finished

			if finished:
				break

	def precompute_final_from_intermediate(self, content):
		for resp in self.completion_with_backoff(
			model="gpt-3.5-turbo", messages=[
				{"role": "system", "content": GPT_INTERMEDAITE_TO_FINAL_SYSTEM_SUMMARY_PROMPT},
				{"role": "user", "content": content}
			], stream=True
		):
			finished = resp.choices[0].finish_reason is not None
			delta_content = "\n" if (finished) else resp.choices[0].delta.content

			sys.stdout.write(delta_content)
			sys.stdout.flush()
			yield delta_content, finished

			if finished:
				break

class LLaMABackend(AIBackend):
	def __init__(self):
		self.model_name_or_path = "TheBloke/Llama-2-7b-Chat-GPTQ"
		self.model = AutoModelForCausalLM.from_pretrained(self.model_name_or_path,
											device_map="cuda:0",
                                            trust_remote_code=False,
                                            revision="main")
		self.tokenizer = AutoTokenizer.from_pretrained(self.model_name_or_path, use_fast=True)
		self.streamer = TextIteratorStreamer(self.tokenizer)

	def get_summary_from_text(self):
		pass
	def get_summary_from_intermediate(self):
		pass
	def get_quiz_from_text(self):
		pass
	def get_quiz_from_intermediate(self):
		pass

	def precompute_intermediate_from_text(self, sliced_text):
		# inserted_input_ids = torch.cat([
		# 	self.tokenizer(PROMPT_TEMPLATE_INTERMEDIATE, return_tensors='pt').input_ids[:, :-4],
		# 	self.tokenizer(sliced_text, return_tensors='pt').input_ids,
		# 	self.tokenizer(PROMPT_TEMPLATE_INTERMEDIATE,return_tensors='pt').input_ids[:, -4:]],
		# 	dim=1).cuda()

		inputs = self.tokenizer(
			PROMPT_TEMPLATE_INTERMEDIATE_FRONT + sliced_text + PROMPT_TEMPLATE_INTERMEDIATE_BACK,
			return_tensors='pt').to('cuda:0')
		generation_kwargs = dict(inputs, streamer=self.streamer, max_new_tokens=512)
		thread = Thread(target=self.model.generate, kwargs=generation_kwargs)
		thread.start()
		for new_text in self.streamer:
			sys.stdout.write(new_text)
			sys.stdout.flush()
			yield new_text, False
		yield "\n", True

	def precompute_intermediate_from_intermediate(self, content):
		inputs = self.tokenizer(
			PROMPT_TEMPLATE_INTERMEDIATE_FRONT + content + PROMPT_TEMPLATE_INTERMEDIATE_BACK,
			return_tensors='pt').to('cuda:0')
		generation_kwargs = dict(inputs, streamer=self.streamer, max_new_tokens=512)
		thread = Thread(target=self.model.generate, kwargs=generation_kwargs)
		thread.start()
		for new_text in self.streamer:
			sys.stdout.write(new_text)
			sys.stdout.flush()
			yield new_text, False
		yield "\n", True

	def precompute_final_from_intermediate(self, content):
		inputs = self.tokenizer(
			PROMPT_TEMPLATE_FINAL_FRONT + content + PROMPT_TEMPLATE_FINAL_BACK,
			return_tensors='pt').to('cuda:0')
		generation_kwargs = dict(inputs, streamer=self.streamer, max_new_tokens=512)
		thread = Thread(target=self.model.generate, kwargs=generation_kwargs)
		thread.start()
		for new_text in self.streamer:
			sys.stdout.write(new_text)
			sys.stdout.flush()
			yield new_text, False
		yield "\n", True
