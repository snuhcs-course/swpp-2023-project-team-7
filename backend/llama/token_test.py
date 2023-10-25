from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
import torch

model_name_or_path = "TheBloke/Llama-2-7b-Chat-GPTQ"
# # To use a different branch, change revision
# # For example: revision="gptq-4bit-64g-actorder_True"
# model = AutoModelForCausalLM.from_pretrained(model_name_or_path,
#                                              device_map="auto",
#                                              trust_remote_code=False,
#                                              revision="main")

tokenizer = AutoTokenizer.from_pretrained(model_name_or_path, use_fast=True)

def split_large_text(story):
	tokens = tokenizer(story, return_tensors='pt').input_ids
	sliced_tensors = []	
	MAX_SIZE = 3500
	for start_idx in range(0, tokens.shape[1], MAX_SIZE):
		sliced_tensors.append(tokens[:, start_idx:start_idx+MAX_SIZE])

	return sliced_tensors
		
prompt = open("the_open_boat.txt", "r").read()
prompt_template=f'''[/INST]'''

prompt_template_tokens = tokenizer(prompt_template, return_tensors='pt').input_ids
# show decoded token
for token in prompt_template_tokens[0]:
  print(tokenizer.decode(token))
