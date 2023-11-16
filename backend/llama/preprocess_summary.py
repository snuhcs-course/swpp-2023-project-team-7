import sys
import pickle
from custom_type import Summary
import tiktoken
import openai

tokenizer = tiktoken.get_encoding("cl100k_base")
MAX_SIZE = 3900

INTERMEDIATE_SYSTEM_PROMPT = '''
"Hello, ChatGPT. I have a passage from a novel that I need help with. 
It's quite long and detailed, and I'm looking to create a summary of the larger text it's a part of. 
Can you assist me by identifying and extracting the most crucial bullet points from this passage? 
These points should capture key events, character developments, themes, or any significant literary elements that are essential to the overall narrative and its context in the larger story. 
Thank you!"
'''

# FINAL_SYSTEM_SUMMARY_PROMPT='''
# Hello again, ChatGPT. 
# Earlier, you helped me by extracting crucial bullet points from a passage of a novel. 
# Now, I need your assistance in using these bullet points to create an overarching summary of the entire novel. 
# The summary should integrate these key points in a cohesive and fluid manner, highlighting the main plot, character arcs, themes, and any significant literary elements that define the novel. 
# The aim is to capture the essence and narrative flow of the book, providing a comprehensive yet concise overview. 
# Could you please help me formulate this into a well-structured summary? Thank you!
# '''

FINAL_SYSTEM_SUMMARY_PROMPT = '''
You will be provided with several bullet points that outline key aspects of a story. Your task is to synthesize these points into a coherent and concise summary that captures the most crucial elements necessary for understanding the story’s overall plot. Your summary should make it easy for a reader to grasp the main ideas, themes, and developments within the story.
Read the bullet points carefully: Carefully analyze each bullet point to understand the fundamental components of the story, such as the main events, character motivations, conflicts, and resolutions.

Crafting the Summary:
Your summary should be well-organized, flowing seamlessly from one point to the next to create a cohesive understanding of the story.
Focus on conveying the key elements that are central to the story’s plot and overall message.
Avoid including overly detailed or minor points that do not significantly contribute to understanding the core plot.

Length and Detail:
Aim for a summary that is concise yet comprehensive enough to convey the essential plot points.
Ensure that the summary is not overly lengthy or cluttered with less pertinent details.

Final Touches:
Review your summary to ensure that it accurately represents the main ideas and themes presented in the bullet points.
Ensure that the language used is clear and easily understandable.
'''

def split_large_text(story):
    tokens = tokenizer.encode(story)

    sliced_lists = []
    for start_idx in range(0, len(tokens), MAX_SIZE):
        end_idx = min(start_idx+MAX_SIZE, len(tokens))
        sliced_story = {
            "sliced_text": tokenizer.decode(tokens[start_idx:end_idx]), "start_idx": start_idx, "end_idx": end_idx-1
        }
        sliced_lists.append(sliced_story)
        print(f"split_large_text start_idx: {start_idx}, end_idx: {end_idx}")
    return sliced_lists


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
    content = '\n'.join(summary_content_list)

    if is_intermediate:
        response = ""
        for resp in openai.ChatCompletion.create(
            model="gpt-3.5-turbo", messages=[
                {"role": "system", "content": INTERMEDIATE_SYSTEM_PROMPT},
                {"role": "user", "content": content}
            ], stream=True
        ):
            finished = resp.choices[0].finish_reason is not None
            delta_content = "\n" if (finished) else resp.choices[0].delta.content
            response += delta_content

            sys.stdout.write(delta_content)
            sys.stdout.flush()
            if finished:
                break
    else:
        response = ""
        for resp in openai.ChatCompletion.create(
            model="gpt-3.5-turbo", messages=[
                {"role": "system", "content": FINAL_SYSTEM_SUMMARY_PROMPT},
                {"role": "user", "content": content}
            ], stream=True
        ):
            finished = resp.choices[0].finish_reason is not None
            delta_content = "\n" if (finished) else resp.choices[0].delta.content
            response += delta_content

            sys.stdout.write(delta_content)
            sys.stdout.flush()
            if finished:
                break

    reduced_summary = Summary(summary_content=response,
                              start_idx=reduced_start_idx, end_idx=reduced_end_idx, children=summary_list)
    for summary in summary_list:
        summary.parent = reduced_summary

    return reduced_summary


def reduce_summaries_list(summaries_list):
    while len(summaries_list) > 1:
        double_paired_list = split_list(summaries_list)
        summaries_list = [reduce_multiple_summaries_to_one(double_pair, is_intermediate=(
            len(summaries_list) > 3)) for double_pair in double_paired_list]
    return summaries_list[0]


async def generate_summary_tree(summary_path_url, story):
    summaries_list = []
    sliced_text_dict_list = split_large_text(story)
    for prompt in sliced_text_dict_list:
        response = ""
        for resp in openai.ChatCompletion.create(
            model="gpt-3.5-turbo", messages=[
                {"role": "system", "content": INTERMEDIATE_SYSTEM_PROMPT},
                {"role": "user", "content": prompt["sliced_text"]}
            ], stream=True
        ):
            finished = resp.choices[0].finish_reason is not None
            delta_content = "\n" if (finished) else resp.choices[0].delta.content
            response += delta_content

            sys.stdout.write(delta_content)
            sys.stdout.flush()
            if finished:
                break 

        first_level_summary = Summary(summary_content=response,
                                start_idx=prompt["start_idx"],
                                end_idx=prompt["end_idx"])
        summaries_list.append(first_level_summary)

    single_summary = reduce_summaries_list(summaries_list)
    with open(summary_path_url, 'wb') as pickle_file:
        pickle.dump(single_summary, pickle_file)


def main():
    story_path = sys.argv[1]
    story = open(story_path, "r").read()
    summaries_list = []

    print("\n\n*** Generate:")
    sliced_text_dict_list = split_large_text(story)
    for prompt in sliced_text_dict_list:
        response = ""
        for resp in openai.ChatCompletion.create(
            model="gpt-3.5-turbo", messages=[
                {"role": "system", "content": INTERMEDIATE_SYSTEM_PROMPT},
                {"role": "user", "content": prompt["sliced_text"]}
            ], stream=True
        ):
            finished = resp.choices[0].finish_reason is not None
            delta_content = "\n" if (finished) else resp.choices[0].delta.content
            response += delta_content

            sys.stdout.write(delta_content)
            sys.stdout.flush()
            if finished:
                break 

        first_level_summary = Summary(summary_content=response,
                                start_idx=prompt["start_idx"],
                                end_idx=prompt["end_idx"])
        summaries_list.append(first_level_summary)

    single_summary = reduce_summaries_list(summaries_list)

    summary_tree_path = f"{story_path.split('.')[0]}_summary.pkl"
    with open(summary_tree_path, 'wb') as pickle_file:
        pickle.dump(single_summary, pickle_file)


if __name__ == "__main__":
    main()
