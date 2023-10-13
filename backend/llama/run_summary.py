from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline, TextStreamer
import torch

model_name_or_path = "TheBloke/Llama-2-7b-Chat-GPTQ"
model = AutoModelForCausalLM.from_pretrained(model_name_or_path,
                                             device_map="auto",
                                             trust_remote_code=False,
                                             revision="main")

tokenizer = AutoTokenizer.from_pretrained(model_name_or_path, use_fast=True)
streamer = TextStreamer(tokenizer)

MAX_SIZE = 2900
PROMPT_TEMPLATE=f'''[INST] <<SYS>>
You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, while being safe.  Your answers should not include any harmful, unethical, racist, sexist, toxic, dangerous, or illegal content. Please ensure that your responses are socially unbiased and positive in nature. If a question does not make any sense, or is not factually coherent, explain why instead of answering something not correct. If you don't know the answer to a question, please don't share false information. Your main purpose as an assitant however, is to provide a clear summary of a given text. When you are given a large amount of text, try to extract the most important points that are relevant for understanding the plot of the story. Focus more on the facts rather than the interpretations. Also note that for the summary, you will not be getting a response from the user.
<</SYS>>[/INST]'''
PROMPT_TEMPLATE_TOKENS = tokenizer(PROMPT_TEMPLATE, return_tensors='pt').input_ids

def split_large_text(story):
	tokens = tokenizer(story, return_tensors='pt').input_ids
	sliced_tensors = []	
	for start_idx in range(0, tokens.shape[1], MAX_SIZE):
		sliced_tensors.append(tokens[:, start_idx:start_idx+MAX_SIZE])
	return sliced_tensors
		
def main():
	story = open("the_open_boat.txt", "r").read()

	print("\n\n*** Generate:")
	sliced_tokens = split_large_text(story)
	for prompt in sliced_tokens:
		inserted_input_ids = torch.cat([PROMPT_TEMPLATE_TOKENS[:,:-4], prompt, PROMPT_TEMPLATE_TOKENS[:,-4:]], dim=1).cuda()
		output = model.generate(inputs=inserted_input_ids, streamer=streamer, temperature=0.7, do_sample=True, top_p=0.95, top_k=40, max_new_tokens=512)

if __name__ == "__main__":
	main()
