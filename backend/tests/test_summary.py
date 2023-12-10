import pytest
import sys
sys.path.append('/home/swpp/swpp-2023-project-team-7/backend/')
from llama.preprocess_summary import (
    split_large_text, split_list, MAX_SIZE, 
    reduce_multiple_summaries_to_one, reduce_summaries_list, 
    generate_summary_tree, update_summary_path_url, get_number_of_inferences
)
from llama.custom_type import Summary, ProxyAIBackend, GPT4Backend, GPT3Backend
import random
import string
import tiktoken

tokenizer = tiktoken.get_encoding("cl100k_base")
def test_split_text():
	random_letters = random.choices(string.ascii_uppercase, k=150000)
	split_large = split_large_text("".join(random_letters))
	for split in split_large:
		assert (split['end_idx']-split['start_idx'])<= MAX_SIZE
	# print(type([split for split in split_large]))
	# breakpoint()
	assert sum([(end_start[0]-end_start[1]+1) for end_start in [(split['end_idx'], split['start_idx']) for split in split_large]]) == len(tokenizer.encode("".join(random_letters)))

def test_find_included_summary():
	summary1 = Summary(
		start_idx=0,
		end_idx=49,
		summary_content="summary1"
	)
	summary1_1 = Summary(
		parent=summary1,
		start_idx=0,
		end_idx=19,
		summary_content="summary1_1"
	)
	summary1_2 = Summary(
		parent=summary1,
		start_idx=20,
		end_idx=49,
		summary_content="summary1_2"
	)
	summary1.children = [summary1_1, summary1_2]
	summary1_1_1 = Summary(
		parent=summary1_1,
		start_idx=0,
		end_idx=9,
		summary_content="summary1_1_1"
	)
	summary1_1_2 = Summary(
		parent=summary1_1,
		start_idx=10,
		end_idx=19,
		summary_content="summary1_1_2"
	)
	summary1_1.children = [summary1_1_1, summary1_1_2]
	summary1_2_1 = Summary(
		parent=summary1_2,
		start_idx=20,
		end_idx=29,
		summary_content="summary1_2_1"
	)
	summary1_2_2 = Summary(
		parent=summary1_2,
		start_idx=30,
		end_idx=39,
		summary_content="summary1_2_2"
	)
	summary1_2_3 = Summary(
		parent=summary1_2,
		start_idx=40,
		end_idx=49,
		summary_content="summary1_2_3"
	)
	summary1_2.children = [summary1_2_1, summary1_2_2, summary1_2_3]

	#              		 summary1
	# 				 		/              \
	# 					/                 \
	# 	summary1_1                       summary1_2
	# 	 /       \                 /         \         \
	# 	/         \               /           \         \
	# summary1_1_1 summary1_1_2 summary1_2_1 summary1_2_2 summary1_2_3

	# find leaf summary
	assert summary1.find_leaf_summary(0) == summary1_1_1
	assert summary1.find_leaf_summary(10) == summary1_1_2
	assert summary1.find_leaf_summary(20) == summary1_2_1
	assert summary1.find_leaf_summary(25) == summary1_2_1
	assert summary1.find_leaf_summary(30) == summary1_2_2
	assert summary1.find_leaf_summary(35) == summary1_2_2
	assert summary1.find_leaf_summary(39) == summary1_2_2
	assert summary1.find_leaf_summary(45) == summary1_2_3
	assert summary1.find_leaf_summary(49) == summary1_2_3
	assert summary1.find_leaf_summary(50) == None
	
	# find included summary
	assert set(summary1.find_included_summaries(summary1_1_1)) == set([])
	assert set(summary1.find_included_summaries(summary1_1_2)) == set([summary1_1_1])
	assert set(summary1.find_included_summaries(summary1_2_1)) == set([summary1_1])
	assert set(summary1.find_included_summaries(summary1_2_2)) == set([summary1_1, summary1_2_1])
	assert set(summary1.find_included_summaries(summary1_2_3)) == set([summary1_1, summary1_2_1, summary1_2_2])

def test_split_list():
    # Test case with an even-sized list
    list1 = [1, 2, 3, 4, 5, 6]
    expected1 = [[1, 2], [3, 4], [5, 6]]
    assert split_list(list1) == expected1, "Failed on even-sized list"
    print("first case passed")

    # Test case with an odd-sized list
    list2 = [1, 2, 3, 4, 5]
    expected2 = [[1, 2], [3, 4, 5]]
    assert split_list(list2) == expected2, "Failed on odd-sized list"
    print("second case passed")

    # Test case with an empty list
    list3 = []
    expected3 = []
    assert split_list(list3) == expected3, "Failed on empty list"
    print("third case passed")

    # Test case with a list smaller than split size
    list4 = [1]
    expected4 = [[1]]
    assert split_list(list4) == expected4, "Failed on list smaller than split size"
    print("fourth case passed")

    # Test case with a string list
    list5 = ["a", "b", "c", "d", "e"]
    expected5 = [["a", "b"], ["c", "d", "e"]]
    assert split_list(list5) == expected5, "Failed on string list"
    print("fifth case passed")

    # Test case with a mixed-type list
    list6 = [1, "b", 3.0, True]
    expected6 = [[1, "b"], [3.0, True]]
    assert split_list(list6) == expected6, "Failed on mixed-type list"
    print("sixth case passed")

def test_get_number_of_inferences():
	# Test case with an even-sized list
	list1 = [1, 2, 3, 4, 5, 6]
	expected1 = 6 + 3 + 1
	assert get_number_of_inferences(len(list1)) == expected1, "Failed on even-sized list"
	print("first case passed")

	# Test case with an odd-sized list
	list2 = [1, 2, 3, 4, 5]
	expected2 = 5 + 2 + 1
	assert get_number_of_inferences(len(list2)) == expected2, "Failed on odd-sized list"
	print("second case passed")

	# Test case with an empty list
	list3 = []
	expected3 = 0
	assert get_number_of_inferences(len(list3)) == expected3, "Failed on empty list"
	print("third case passed")

	# Test case with a list smaller than split size
	list4 = [1]
	expected4 = 1
	assert get_number_of_inferences(len(list4)) == expected4, "Failed on list smaller than split size"
	print("fourth case passed")

def test_proxy_pattern():
	ai_backend = ProxyAIBackend(GPT4Backend())
	assert type(ai_backend.summary_generator) == GPT4Backend
	ai_backend.summary_generator = GPT3Backend()
	assert type(ai_backend.summary_generator) == GPT3Backend

