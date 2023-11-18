import sys
from os import path
import pickle
import openai
import tiktoken

tokenizer = tiktoken.get_encoding("cl100k_base")

SYSTEM_QUIZ_PROMPT = '''
You will be presented with a series of bullet points summarizing key elements of a story. Your task is to generate questions that are crucial for understanding the overall plot and essential aspects of the story. Generate a minimum of 2 and a maximum of 10 questions, ensuring that the questions you choose to create are deeply rooted in the comprehension and analysis of the story's plot, characters, and themes.

Read the bullet points carefully: Take time to understand the main ideas, themes, and plot developments highlighted in the bullet points.

Generate Questions:

First, write number of questions you want to generate.
Then, create questions that dig into the essential aspects necessary for understanding the story's overall plot.
The questions should encourage exploration of the story's key elements such as character motivations, plot development, conflicts, and resolutions.
Avoid asking overly detailed questions that do not contribute significantly to the understanding of the story’s main plot or themes.
Number of Questions:

Generate at least 2 questions that target the most critical aspects of the story.
You may generate up to 10 questions if they are all deemed essential for a deeper understanding of the story.
Question Format:

Ensure that the questions are open-ended to promote deeper thinking and analysis.
Format the questions clearly and concisely.
Be sure to provide the answers as well.

Format:
Number of Questions: <number of questions>
1Q: <question 1>
1A: <answer 1>
2Q: <question 2>
2A: <answer 2>
'''

def get_quizzes(progress, book_content_url, summary_tree_url):
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
    tokenized_read_content = tokenizer.encode(read_content)
    word_index = len(tokenized_read_content)-1
    
    # generate new quiz
    leaf = summary_tree.find_leaf_summary(word_index=word_index)
    available_summary_list = summary_tree.find_included_summaries(leaf)

    content = "\n\n".join([summary.summary_content for summary in available_summary_list])
    content += "\n\n" + book_content[leaf.end_idx:word_index]

    for resp in openai.ChatCompletion.create(
        model="gpt-4", messages=[
            {"role": "system", "content": SYSTEM_QUIZ_PROMPT},
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
    # key = None
    # for quiz, quiz_len in get_quizzes(progress=10880 / len(book_content), book_id=1):
    #     print(quiz, quiz_len)
    pass