import nltk
from jnius import PythonJavaClass, java_method

class PythonInterface(PythonJavaClass):
    __javainterfaces__ = ['com.example.PythonInterface']

    def __init__(self):
        super().__init__()

    @java_method('(Ljava/lang/String;)Ljava/lang/String;')
    def callPythonMethod(self, sentence):
        # Python kodunu burada çağırabilirsiniz
        print("Python metodunu çağırma")

        # Türkçe stopwords listesini indirin
        nltk.download('stopwords')

        # Olumlu ve olumsuz kelimeler listesi
        olumlu_kelimeler = ["harika", "beğendim", "iyi"]
        olumsuz_kelimeler = ["kötü", "berbat", "beğenmedim"]

        # Metni küçük harfe çevirin
        sentence = sentence.lower()

        # Cümleyi kelimelere ayırın
        kelimeler = nltk.word_tokenize(sentence)

        # Olumlu ve olumsuz kelimelerin sayısını sayın
        olumlu_sayisi = sum(1 for kelime in kelimeler if kelime in olumlu_kelimeler)
        olumsuz_sayisi = sum(1 for kelime in kelimeler if kelime in olumsuz_kelimeler)

        # Olumlu ve olumsuz kelimelerin sayısına göre cümlenin olumlu/olumsuz olduğunu belirleyin
        if olumlu_sayisi > olumsuz_sayisi:
            return "Olumlu"
        elif olumlu_sayisi < olumsuz_sayisi:
            return "Olumsuz"
        else:
            return "Nötr"
