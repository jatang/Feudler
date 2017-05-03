from itertools import chain
from nltk.corpus import wordnet
import sqlite3

db_file = '../../../data/similar_words.sqlite3'
vocab_file = '../../../data/wiki-100k.txt'

# gets the vocab
file_contents = open(vocab_file, 'r')
vocab = [line.split('\n')[0].lower() for line in file_contents.readlines() if line[0] != '#']
print 'Read file.'

conn = sqlite3.connect(db_file)
c = conn.cursor()

# don't call these if the table already exists
c.execute('CREATE TABLE IF NOT EXISTS similar (word1 text, word2 text) ')
c.execute('CREATE INDEX IF NOT EXISTS idx_word ON similar (word1)')
print 'Created table and index.'

def synonym_set(word):
    try:
        synonyms = wordnet.synsets(word)
        return set(chain.from_iterable([word.lemma_names() for word in synonyms]))
    except:
        return set()

def add_similar_words(words, letter):
    words_letter = [word for word in words if word.lower()[0] == letter]
    tups = []
    count = 0
    print "Total for " + str(letter) + ": " + str(len(words_letter))
    for word in words_letter:
        count += 1
        if count % 1000 == 0:
            print count
        similar = synonym_set(word)
        similar = similar.intersection(vocab)
        tups = [(word, other) for other in similar if other != word]
        c.executemany('INSERT INTO similar VALUES (?, ?)', tups)

# change the string to change which letters should be loaded into the database.
# doing it this way for convenience, if the script needs to be cancelled at some point.
for letter in 'abcdefghijklmnopqrstuvwxyz':
    t = add_similar_words(vocab, letter)
    conn.commit()

conn.commit()
conn.close()
