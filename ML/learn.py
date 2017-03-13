import sklearn
import numpy
from sklearn import tree
from numpy import genfromtxt

features = genfromtxt('examples.csv', delimiter=',')
labels = genfromtxt('targets.csv', delimiter=',')
labels2 = genfromtxt('targets2.csv', delimiter=',')
clf = tree.DecisionTreeClassifier()
clf = clf.fit(features, labels)
print clf.predict([[-0.26,1.63,2.31,0.62,0.66,1.2,141.17]])