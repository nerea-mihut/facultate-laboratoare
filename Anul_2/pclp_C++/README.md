# Programarea Calculatoarelor și Limbaje de Programare (PCLP) - C++

Acest folder conține exerciții și aplicații practice dezvoltate pentru cursul de **PCLP**, făcând tranziția către concepte structurale și introducerea în programarea orientată pe obiecte folosind C++.

**Structura Proiectului & Concepte Abordate**

* **`produse_magazin.cpp`** - Gestiune produse (Fizice, Digitale, Perisabile) folosind clase abstracte, metode virtuale pure, polimorfism și supradefinirea operatorilor (`+`, `==`) pentru un tablou dinamic de pointeri.
* **`perechi_template.cpp`** - Implementare de șabloane de clase (`template <class T1, class T2>`) pentru crearea de perechi generice de date și supradefinirea operatorului de inserție în flux (`<<`).
* **`calcul_matematic.cpp`** - Exercițiu introductiv de bază pentru prelucrarea datelor dintr-un tablou static și operații matematice simple.
* **`cont_bancar.cpp`** - Ierarhie de clase pentru conturi bancare (Curent, Economii) cu gestiune de istoric tranzacții, supradefinirea operatorilor unari și binari (`--`, `+`, `<<`) și legătură dinamică.
* **`media_playlist.cpp`** - Aplicație multimedia polimorfică (Melodii, Podcasturi, Audiobook-uri) gestionată printr-un container de tip playlist cu supradefinirea operatorilor (`+=`, `[]`).
* **`vehicule_transport.cpp`** - Calculul costurilor de transport pe kilometru pentru diverse tipuri de vehicule (Autoturism, Autobuz, Camion) folosind clase derivate și pointeri la clasa de bază.
* **`angajat_persoana_job.cpp`** - Exemplu de moștenire multiplă (clasa `Angajat` moștenește simultan din `Persoana` și `Job`), cu alocare dinamică și destructori virtuali pentru prevenirea pierderilor de memorie.

**Cum se rulează?**
1. Deschideți fișierele `.cpp` într-un mediu de dezvoltare compatibil C++ (ex. Code::Blocks, Visual Studio).
2. Compilați și rulați fișierul dorit pentru a testa execuția.
