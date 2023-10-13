import pytest
import sys
sys.path.append('/home/swpp/swpp-2023-project-team-7/backend/')
from llama.run_summary import split_large_text, MAX_SIZE

def test_split_text():
	with open("/home/swpp/swpp-2023-project-team-7/backend/llama/the_open_boat.txt") as f:
		story = f.read()
		split_large = split_large_text(story)
		for split in split_large:
			assert split.shape[1] <= MAX_SIZE
