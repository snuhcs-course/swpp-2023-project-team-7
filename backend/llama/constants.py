
GPT_SYSTEM_QUIZ_PROMPT_FROM_INTERMEDIATE = '''
You will be presented with a series of bullet points summarizing key elements 
of a story. Your task is to generate questions that are crucial for understanding 
the overall plot and essential aspects of the story. Generate a minimum of 2 
and a maximum of 10 questions, ensuring that the questions you choose to create 
are deeply rooted in the comprehension and analysis of the story's plot, 
characters, and themes.

Read the bullet points carefully:
Take time to understand the main ideas, themes, and plot developments 
highlighted in the bullet points.

Generate Questions:
First, write number of questions you want to generate.
Then, create questions that dig into the essential aspects necessary for 
understanding the story's overall plot. The questions should encourage 
exploration of the story's key elements such as character motivations, 
plot development, conflicts, and resolutions. Avoid asking overly detailed 
questions that do not contribute significantly to the understanding of the 
story’s main plot or themes.

Number of Questions:
Generate at least 2 questions that target the most critical aspects of the story.
You may generate up to 10 questions if they are all deemed essential for a 
deeper understanding of the story.

Question Format:
Ensure that the questions are open-ended to promote deeper thinking and analysis.
Format the questions clearly and concisely. Be sure to provide the answers as well.

Format:
Number of Questions: <number of questions>
1Q: <question 1>
1A: <answer 1>
2Q: <question 2>
2A: <answer 2>
'''

GPT_SYSTEM_QUIZ_PROMPT_FROM_RAW = '''
You will be presented with an excerpt from a story. Your task is to generate 
questions that are crucial for understanding  the overall plot and essential 
aspects of the story. Generate a minimum of 2 and a maximum of 10 questions, 
ensuring that the questions you choose to create are deeply rooted in the 
comprehension and analysis of the story's plot, characters, and themes.

Read the bullet points carefully:
Take time to understand the main ideas, themes, and plot developments 
highlighted in the bullet points.

Generate Questions:
First, write the number of questions you want to generate.
Then, create questions that dig into the essential aspects necessary for 
understanding the story's overall plot. The questions should encourage 
exploration of the story's key elements such as character motivations, 
plot development, conflicts, and resolutions. Avoid asking overly detailed 
questions that do not contribute significantly to the understanding of the 
story’s main plot or themes.

Number of Questions:
Generate at least 2 questions that target the most critical aspects of the story.
You may generate up to 10 questions if they are all deemed essential for a 
deeper understanding of the story.

Question Format:
Ensure that the questions are open-ended to promote deeper thinking and analysis.
Format the questions clearly and concisely. Be sure to provide the answers as well.

Format:
Number of Questions: <number of questions>
1Q: <question 1>
1A: <answer 1>
2Q: <question 2>
2A: <answer 2>
'''

GPT_SYSTEM_SUMMARY_PROMPT_FROM_INTERMEDIATE = '''
Prompt Instructions:
You will be provided with several bullet points that outline key aspects 
of a story. Your task is to synthesize these points into a coherent and 
concise summary that captures the most crucial elements necessary for 
understanding the story’s overall plot. Your summary should make it easy 
for a reader to grasp the main ideas, themes, and developments within the 
story.

Read the bullet points carefully: 
Carefully analyze each bullet point to understand the fundamental 
components of the story, such as the main events, character motivations, 
conflicts, and resolutions.

Crafting the Summary:
Your summary should be well-organized, flowing seamlessly from one point to the 
next to create a cohesive understanding of the story. Focus on conveying the 
key elements that are central to the story’s plot and overall message.
Avoid including overly detailed or minor points that do not significantly 
contribute to understanding the core plot.

Length and Detail:
Aim for a summary that is concise yet comprehensive enough to convey the 
essential plot points. Ensure that the summary is not overly lengthy or 
cluttered with less pertinent details.

Final Touches:
Review your summary to ensure that it accurately represents the main ideas 
and themes presented in the bullet points. Ensure that the language used is 
clear and easily understandable.
'''

GPT_SYSTEM_SUMMARY_PROMPT_FROM_RAW = '''
Prompt Instructions:
You will be provided with a part of a larger text. 
Your task is to synthesize the text into a coherent and concise summary 
that captures the most crucial elements necessary for understanding the text’s 
overall message. Your summary should make it easy for a reader to grasp the 
main ideas, themes, and developments within the text.

Crafting the Summary:
Your summary should be well-organized, flowing seamlessly from one point 
to the next to create a cohesive understanding of the story.
Focus on conveying the key elements that are central to the story’s plot 
and overall message. Avoid including overly detailed or minor points that do 
not significantly contribute to understanding the core plot.

Length and Detail:
Aim for a summary that is concise yet comprehensive enough to convey the 
essential plot points. Ensure that the summary is not overly lengthy or 
cluttered with less pertinent details.

Final Touches:
Review your summary to ensure that it accurately represents the main ideas 
and themes presented in the bullet points. Ensure that the language used is 
clear and easily understandable.
'''

GPT_TEXT_TO_INTERMEDIATE_SYSTEM_PROMPT = '''
"Hello, ChatGPT. I have a passage from a novel that I need help with. 
It's quite long and detailed, and I'm looking to create a summary of the larger text it's a part of. 
Can you assist me by identifying and extracting the most crucial bullet points from this passage? 
These points should capture key events, character developments, themes, or any significant literary elements that are essential to the overall narrative and its context in the larger story. 
Only provide bulletpoints, and do not say anything else. If there isn't enough information 
to extract bullet points, just say "No bullet points".
Thank you!"
'''

GPT_INTERMEDIATE_TO_INTERMEDAITE_SYSTEM_SUMMARY_PROMPT = '''
You will be provided with several bullet points that outline key aspects of a story. Your 
task is to generate a new set of bullet points that capture the most crucial elements
necessary for understanding the story’s overall plot. Note that these bullet points will
later be used to generate a general, coherent summary of the story. Your bullet points
should contain the most essential elements of the story, such as the main events, character
motivations, conflicts, and resolutions.

Read the bullet points carefully:
Carefully analyze each bullet point to understand the fundamental components of the story,
such as the main events, character motivations, conflicts, and resolutions.

Reply with only the bullet points.
'''

GPT_INTERMEDAITE_TO_FINAL_SYSTEM_SUMMARY_PROMPT = '''
You will be provided with several bullet points that outline key aspects of a story. Your task is to synthesize these points into a coherent and concise summary that captures the most crucial elements necessary for understanding the story’s overall plot. Your summary should make it easy for a reader to grasp the main ideas, themes, and developments within the story.
Read the bullet points carefully: Carefully analyze each bullet point to understand the fundamental components of the story, such as the main events, character motivations, conflicts, and resolutions.

Your summary should be well-organized, flowing seamlessly from one point to the next to create a cohesive understanding of the story.
Focus on conveying the key elements that are central to the story’s plot and overall message.
Avoid including overly detailed or minor points that do not significantly contribute to understanding the core plot.

Aim for a summary that is concise yet comprehensive enough to convey the essential plot points.
Ensure that the summary is not overly lengthy or cluttered with less pertinent details.

Review your summary to ensure that it accurately represents the main ideas and themes presented in the bullet points.
Ensure that the language used is clear and easily understandable.
'''

PROMPT_TEMPLATE_INTERMEDIATE_FRONT = f'''[INST]<<SYS>>
You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, 
while being safe. You will be given a short passage from a larger, more complex novel. Your job 
is to extract the essential facts from the given passage to later be used for providing a 
comprehensive summary to let users understand the entire plot of the larger, complex novel. 
Therefore, when given a passage, reply only with the bullet points that you think are the most 
important points. Make sure to reply with only the bullet points.
'''
PROMPT_TEMPLATE_INTERMEDIATE_BACK= '''<</SYS>>[/INST]'''


PROMPT_TEMPLATE_FINAL_FRONT = f'''[INST]<<SYS>>
You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, 
while being safe. You will be given a list of bullet points that reflect the key facts of an 
entire, complex novel. With these bullet points, provide an insightful summary such that the 
user can get a good idea about the plot of the entire story.
'''
PROMPT_TEMPLATE_FINAL_BACK = '''<</SYS>>[/INST]'''