import pytest
import sys
sys.path.append('/home/swpp/swpp-2023-project-team-7/backend/')
from llama.run_summary import split_large_text, MAX_SIZE
from llama.custom_type import Summary

def test_split_text():
	with open("/home/swpp/swpp-2023-project-team-7/backend/llama/the_open_boat.txt") as f:
		story = f.read()
		split_large = split_large_text(story)
		for split in split_large:
			assert split['tokens'].shape[1] <= MAX_SIZE

def test_find_included_summary():
	summary1 = Summary(
		start_idx=0,
		end_idx=40,
		summary_content="summary1"
	)
	summary1_1 = Summary(
		parent=summary1,
		start_idx=0,
		end_idx=20,
		summary_content="summary1_1"
	)
	summary1_2 = Summary(
		parent=summary1,
		start_idx=20,
		end_idx=40,
		summary_content="summary1_2"
	)
	summary1.children = [summary1_1, summary1_2]
	summary1_1_1 = Summary(
		parent=summary1_1,
		start_idx=0,
		end_idx=10,
		summary_content="summary1_1_1"
	)
	summary1_1_2 = Summary(
		parent=summary1_1,
		start_idx=10,
		end_idx=20,
		summary_content="summary1_1_2"
	)
	summary1_1.children = [summary1_1_1, summary1_1_2]
	summary1_2_1 = Summary(
		parent=summary1_2,
		start_idx=20,
		end_idx=30,
		summary_content="summary1_2_1"
	)
	summary1_2_2 = Summary(
		parent=summary1_2,
		start_idx=30,
		end_idx=35,
		summary_content="summary1_2_2"
	)
	summary1_2_3 = Summary(
		parent=summary1_2,
		start_idx=35,
		end_idx=40,
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
	assert summary1.find_leaf_summary(35) == summary1_2_3
	assert summary1.find_leaf_summary(39) == summary1_2_3
	
	# find included summary
	assert set(summary1.find_included_summaries(summary1_1_1)) == set([])
	assert set(summary1.find_included_summaries(summary1_1_2)) == set([summary1_1_1])
	assert set(summary1.find_included_summaries(summary1_2_1)) == set([summary1_1])
	assert set(summary1.find_included_summaries(summary1_2_2)) == set([summary1_1, summary1_2_1])
	assert set(summary1.find_included_summaries(summary1_2_3)) == set([summary1_1, summary1_2_1, summary1_2_2])