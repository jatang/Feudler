from gensim.models.word2vec import Word2Vec
import sqlite3
# need gensim and sqlite3 in python

# change these variables before running the script
bin_file = 'path/to/GoogleNews-vectors-negative300.bin'
db_file = 'path/to/word2vec_db.sqlite3'

model = Word2Vec.load_word2vec_format(bin_file, binary=True)
conn = sqlite3.connect(db_file)
c = conn.cursor()
vocab = model.vocab.keys() # all the words in the model

# considers only single words, not combined phrases like the_book
single_words = {word for word in vocab if ("_" not in word) and ("." not in word) and ("#" not in word)}

# all words that are lowercased
lower_single = {word.lower() for word in single_words}.intersection(single_words)

# all words that are not lowercased
upper_single = single_words - lower_single

# all words that are not lowercased that also don't have a lowercase counterpart in single_words
uniquely_upper = {word for word in upper_single if word.lower() not in lower_single}

lowered_uniquely_upper = set()
uniquely_spelled_upper = set()
for word in uniquely_upper:
    # if two words have the same spelling, it picks one arbitrarily
    if word.lower() not in lowered_uniquely_upper:
        uniquely_spelled_upper.add(word)
        lowered_uniquely_upper.add(word.lower())


# all the words that we want to put in the database
all_needed = lower_single.union(uniquely_spelled_upper) # around 800,000 words, as opposed to original 3M

# don't call these if the table already exists
c.execute('CREATE TABLE embeddings (word text, vector text)')
c.execute('CREATE INDEX idx_word ON embeddings (word)')

def tups_of(words, letter):
    words_letter = [word for word in words if word.lower()[0] == letter]
    tups = []
    count = 0
    print "Total for " + str(letter) + ": " + str(len(words_letter))
    for word in words_letter:
        count += 1
        if count % 1000 == 0:
            print count
        vector = model[word].tolist()
        vector_scientific_notation = ["{:.6e}".format(x) for x in vector];
        vector_str = ""
        if len(vector_scientific_notation) > 0:
            vector_str += vector_scientific_notation[0]
            for i in range(1, len(vector_scientific_notation)):
                vector_str += "," + vector_scientific_notation[i]
        tup = (word.lower(), vector_str)
        tups.append(tup)
    return tups

# change the string to change which letters should be loaded into the database.
# doing it this way for convenience, if the script needs to be cancelled at some point.
for letter in 'abcdefghijklmnopqrstuvwxyz':
    t = tups_of(all_needed, letter)
    c.executemany('INSERT INTO embeddings VALUES (?, ?)', t)

conn.commit()
conn.close()
