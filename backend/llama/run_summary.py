import sys
from os import path
import uuid
import pickle
import openai

SYSTEM_SUMMARY_PROMPT = '''
Prompt Instructions:

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


base_path = path.dirname(path.realpath(__file__))

book_content = open(path.abspath(path.join(base_path, "medium.txt")), "r").read()
sys.path.append(path.abspath(base_path))
single_summary = ""
with open(path.abspath(path.join(base_path, "medium_summary.pkl")), 'rb') as pickle_file:
    single_summary = pickle.load(pickle_file)

def get_summary(progress, book_id):
    """
    generates summary based on the word_index
    :param progress: progress of the book
    :param book_id: book id to generate quiz from
    :param callback: callback function to call when a delta content is generated
    """

    # TODO: load book from database and use instead of book_content
    word_index = int(progress * len(book_content))
    
    leaf = single_summary.find_leaf_summary(word_index=word_index)
    available_summary_list = single_summary.find_included_summaries(leaf)

    content = "\n\n".join([summary.summary_content for summary in available_summary_list])
    content += "\n\n" + book_content[leaf.end_idx:word_index]

    for resp in openai.ChatCompletion.create(
        model="gpt-3.5-turbo", messages=[
            {"role": "system", "content": SYSTEM_SUMMARY_PROMPT},
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

if __name__ == "__main__":
    # main()
    get_summary(10880, 1)