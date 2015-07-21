import numpy as np

f = open('revisit_interval.txt')

data = []
for line in f.readlines():
    data.append([int(a) for a in line.split('\t')])

dt = np.asarray(data).transpose()

for s in dt:
    sa = []
    for index, e in enumerate(s):
        sa = sa + (np.ones(e) * (index+1)).tolist()

    sa = np.asarray(sa)

    print np.median(sa)
    print np.percentile(sa, 25)
    print np.percentile(sa, 75)
    print np.min(sa)
    print np.max(sa)



