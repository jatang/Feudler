from gensim.models.keyedvectors import KeyedVectors
import numpy as np
from sklearn import neighbors
KDTree = neighbors.KDTree

vocab_file = '../../../data/wiki-100k.txt'

# gets the vocab
file_contents = open(vocab_file, 'r')
vocab = [line.split('\n')[0].lower() for line in file_contents.readlines() if line[0] != '#']
print 'Read file.'

bin_file = '../../../data/GoogleNews-vectors-negative300.bin'
model = KeyedVectors.load_word2vec_format(bin_file, binary=True)
print 'Loaded model.'

combined = np.asmatrix(model['science'])
vec2word = {}
print 'Total: ' + str(len(vocab))
count = 0
for word in vocab:
    count += 1
    if count % 100 == 0:
        print count
    try:
        vec = model[word]
        combined = np.concatenate((combined, np.asmatrix(vec)), axis=0)
        vec2word[vec.tostring()] = word
    except:
        pass
print 'Combined.'

tree = KDTree(combined, leaf_size=20) # try different leaf sizes
print 'Made tree.'

def nearest(word):
    dist, ind = tree.query(np.asmatrix(model[word]), k=30)
    top10 = set()
    for index in ind[0][1:]:
        if len(top10) < 10:
            top10.add(vec2word[combined[index].tostring()])
    return top10

#connect to database
import sqlite3
db_file = '../../../data/similar_words.sqlite3'
conn = sqlite3.connect(db_file)
c = conn.cursor()

c.execute('CREATE TABLE IF NOT EXISTS similar (word1 text, word2 text) ')
c.execute('CREATE INDEX IF NOT EXISTS idx_word ON similar (word1)')
print 'Created table and index.'

def add_similar_words(words, letter):
    words_letter = [word for word in words if word.lower()[0] == letter]
    tups = []
    count = 0
    print "Total for " + str(letter) + ": " + str(len(words_letter))
    for word in words_letter:
        count += 1
        if count % 100 == 0:
            print count
        try:
            similar = nearest(word)
            tups = [(word, other) for other in similar if other != word]
            c.executemany('INSERT INTO similar VALUES (?, ?)', tups)
        except:
            pass

# change the string to change which letters should be loaded into the database.
# doing it this way for convenience, if the script needs to be cancelled at some point.
for letter in 'abcdefghijklmnopqrstuvwxyz':
    t = add_similar_words(vocab, letter)
    conn.commit()

conn.commit()
conn.close()
