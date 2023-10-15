from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline, TextStreamer
import torch
import sys
import pickle
from custom_type import Summary

model_name_or_path = "TheBloke/Llama-2-7b-Chat-GPTQ"
model = AutoModelForCausalLM.from_pretrained(model_name_or_path,
                                             device_map="auto",
                                             trust_remote_code=False,
                                             revision="main")

tokenizer = AutoTokenizer.from_pretrained(model_name_or_path, use_fast=True)
streamer = TextStreamer(tokenizer)

MAX_SIZE = 2900
#PROMPT_TEMPLATE=f'''[INST] <<SYS>>
#You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, while being safe. Your main purpose as an assitant however, is to provide a clear summary of a given text. When you are given a large amount of text, try to extract the most important points that are relevant for understanding the plot of the story. Focus more on the facts rather than the interpretations. Also note that for the summary, you will not be getting a response from the user.
#<</SYS>>[/INST]'''

PROMPT_TEMPLATE_INTERMEDIATE=f'''[INST]<<SYS>>
You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, while being safe. You will be given a short passage from a larger, more complex novel. Your job is to extract the essential facts from the given passage to later be used for providing a comprehensive summary to let users understand the entire plot of the larger, complex novel. Therefore, when given a passage, reply only with the bullet points that you think are the most important points.
<</SYS>>[/INST]'''

PROMPT_TEMPLATE_FINAL=f'''[INST]<<SYS>>
You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, while being safe. You will be given a list of bullet points that reflect the key facts of an entire, complex novel. With these bullet points, provide an insightful summary such that the user can get a good idea about the plot of the entire story.
<</SYS>>[/INST]'''

PROMPT_TEMPLATE_TOKENS_INTERMEDIATE = tokenizer(PROMPT_TEMPLATE_INTERMEDIATE, return_tensors='pt').input_ids
PROMPT_TEMPLATE_TOKENS_FINAL = tokenizer(PROMPT_TEMPLATE_FINAL, return_tensors='pt').input_ids

def split_large_text(story):
	res = tokenizer(story, add_special_tokens=False, return_tensors='pt')
	tokens = res.input_ids
	sliced_tensors = []	
	for start_idx in range(0, tokens.shape[1], MAX_SIZE):
		end_idx = min(start_idx+MAX_SIZE, tokens.shape[1])
		start = res.encodings[0].offsets[start_idx][0]
		end = res.encodings[0].offsets[end_idx-1][1]

		sliced_story = {"tokens":tokens[:, start_idx:start_idx+MAX_SIZE], "start_idx":start, "end_idx":end}
		sliced_tensors.append(sliced_story)
	return sliced_tensors

def split_list(input_list):
	if not input_list:
		return []
	split_size = 2
	
	# split the input_list into groups
	num_groups = len(input_list) // split_size
	remainder = len(input_list) % split_size
	output_sizes = []
	output_list = []
	start_idx = 0
	for i in range(num_groups):
		output_sizes.append(split_size)
		start_idx += split_size

	# spread remainder
	if remainder:
		while remainder:
			for i in reversed(range(num_groups)):
				if remainder:
					output_sizes[i] += 1
					start_idx += 1
					remainder -= 1
				else:
					break
	# create output_list
	for i in range(num_groups):
		output_list.append(input_list[:output_sizes[i]])
		input_list = input_list[output_sizes[i]:]

	return output_list

def reduce_multiple_summaries_to_one(summary_list, is_intermediate):
	summary_content_list = [summary.summary_content for summary in summary_list]
	reduced_start_idx = min([summary.start_idx for summary in summary_list])
	reduced_end_idx = max([summary.end_idx for summary in summary_list])

	concat_summaries = '\n'.join(summary_content_list)
	prompt = tokenizer(concat_summaries, return_tensors='pt').input_ids

	if is_intermediate:
		inserted_input_ids = torch.cat([PROMPT_TEMPLATE_TOKENS_INTERMEDIATE[:,:-4], prompt, PROMPT_TEMPLATE_TOKENS_INTERMEDIATE[:,-4:]], dim=1).cuda()
	else:
		inserted_input_ids = torch.cat([PROMPT_TEMPLATE_TOKENS_FINAL[:,:-4], prompt, PROMPT_TEMPLATE_TOKENS_FINAL[:,-4:]], dim=1).cuda()

	output = model.generate(inputs=inserted_input_ids, streamer=streamer, temperature=0.7, do_sample=True, top_p=0.95, top_k=40, max_new_tokens=512)
	decoded_output = tokenizer.decode(output[0]).split("[/INST]")[1].replace("<s>", "").replace("</s>", "")

	reduced_summary = Summary(summary_content=decoded_output, start_idx=reduced_start_idx, end_idx=reduced_end_idx, children=summary_list)
	for summary in summary_list:
		summary.parent = reduced_summary	

	return reduced_summary

def reduce_summaries_list(summaries_list):
	while len(summaries_list) > 1:
		double_paired_list = split_list(summaries_list)
		summaries_list = [reduce_multiple_summaries_to_one(double_pair, is_intermediate=(len(summaries_list)>3)) for double_pair in double_paired_list]	
	return summaries_list[0]
		
def main():
	story_path = sys.argv[1]
	story = open(story_path, "r").read()
	summaries_list = []

	print("\n\n*** Generate:")
	sliced_tokens_dict_list = split_large_text(story)
	for prompt in sliced_tokens_dict_list:
		inserted_input_ids = torch.cat([PROMPT_TEMPLATE_TOKENS_INTERMEDIATE[:,:-4], prompt["tokens"], PROMPT_TEMPLATE_TOKENS_INTERMEDIATE[:,-4:]], dim=1).cuda()
		output = model.generate(inputs=inserted_input_ids, streamer=streamer, temperature=0.7, do_sample=True, top_p=0.95, top_k=40, max_new_tokens=512)
		decoded_output = tokenizer.decode(output[0]).split("[/INST]")[1].replace("<s>", "").replace("</s>", "")

		first_level_summary = Summary(summary_content=decoded_output,
						start_idx=prompt["start_idx"],
						end_idx=prompt["end_idx"])
		summaries_list.append(first_level_summary)

	single_summary = reduce_summaries_list(summaries_list)
	print(single_summary.summary_content)
	with open(f"{story_path.split('.')[0]}_summary.pkl", 'wb') as pickle_file:
		pickle.dump(single_summary, pickle_file)

if __name__ == "__main__":
	main()
