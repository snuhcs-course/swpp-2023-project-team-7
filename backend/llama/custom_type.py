class Summary:
	def __init__(
		self,
		parent=None,
		start_idx=0,
		end_idx=0,
		summary_content=None,
		children=[]
	):
		# parent -> reduced summary
		self.parent = parent
		# child -> unreduced summary
		self.children = children

		self.start_idx = start_idx
		self.end_idx = end_idx
		self.summary_content = summary_content
	
	def find_leaf_summary(self, word_index):
		if len(self.children) == 0:
			return self
		for child in self.children:
			if word_index >= child.start_idx and word_index <= child.end_idx:
				return child.find_leaf_summary(word_index)

	def find_included_summaries(self, child_summary):
		included_summaries = []
		while child_summary.parent is not None:
			siblings = [child for child in child_summary.parent.children if child.end_idx <= child_summary.start_idx]
			included_summaries.extend(siblings)
			child_summary = child_summary.parent

		return included_summaries
	
	def __eq__(self, other):
		if isinstance(other, Summary):
			return self.summary_content == other.summary_content and self.start_idx == other.start_idx and self.end_idx == other.end_idx
		return False

	def __str__(self):
		return f"Summary(start_idx={self.start_idx}, end_idx={self.end_idx}, summary_content={self.summary_content})"
	
	def __hash__(self):
		return hash((self.start_idx, self.end_idx, self.summary_content))
