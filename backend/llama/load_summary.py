import pickle


def main():
	with open("medium_summary.pkl", 'rb') as pickle_file:
		single_summary = pickle.load(pickle_file)
		leaf = single_summary.find_leaf_summary(word_index=10880)
		available_summary_list = single_summary.find_included_summaries(leaf)
		print(available_summary_list)
		breakpoint()

if __name__ == "__main__":
	main()
