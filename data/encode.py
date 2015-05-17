#!/usr/bin/python
# -*- coding: utf-8 -*-
import re

if __name__ == '__main__':
	s1 = {'names': ['baolingfeng', 'lingfengbao', 'blf', '鲍凌峰', 'bao Lingfeng', 'lingfeng bao', 'lingfeng'], 'files': ['raw_action_records_s1_part1.csv', 'raw_action_records_s1_part2.csv']}


def privacy_encode(subject):
	for fname in subject.files:
		fin = open(fname)

		newfname = fname.split('.')[0] + "_encode." + fname.split('.')[1];
		fout = open(newfname, 'w')

		for line in fin.readlines():
			for w in subject.names:
				insensitive_re = re.compile(re.escape(w), re.IGNORECASE)
				line = insensitive_re.sub(w, line)

			fout.write(line+"\n");

		fout.close()
