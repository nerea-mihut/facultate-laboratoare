import random
# rezolvare problema rucsacului
#setarile/parametrii problemei

v_rucsac = 35 #litrii
    #(volum, valoare), fatele de intrare trb sa fie idenitice la fiecare rulare
obiecte = [
    (15, 5),
    (1, 6),
    (2, 2),
    (5, 10),
    (2, 15),
    (20, 20),
    (12, 6)
]

#setarile/parametrii algoritmului

#rep individului
#in cazul acestei prob, ind este un array de bool

pop_size = 10
nr_gen = 20
probabilitate_mutatie = 5 #5% prob mutatie

class Individ:
    def __init__(self, cromozom=[]): # _cromozom ii anonim
        self._cromozom = cromozom #ii privat
        self._fitness_value = None

    def generate_random(self):
        self._cromozom = [random.randint(0,1) for i in range(len(obiecte))] 

    def get_cromozom(self):
        return self._cromozom

    def print_cromozom(self):
        print(self._cromozom)

    def mutate(self):
        for j in range(len(obiecte) - 1):
            has_mutation = random.randint(0, 100) <= probabilitate_mutatie
            if (has_mutation):
                val = self._cromozom[j]
                self._cromozom[j] = 0 if val == 1 else 1 #facem un swap, like daca are mutatie schimbam nr ca s afie modificat

    def get_fitness(self):
        if self._fitness_value == None:
                rez = 0 #suma de valori
                vol = 0 #suma de volume
                for i in range(len(obiecte)):
                    if self._cromozom[i] == 1:
                        rez += obiecte[i][1] #1 este val obiectului 
                        vol += obiecte[i][0] #0 este vol obiectului pt ca obiectu ii rep de un tuplu (vol, val) deci 0 ii vol si 1 ii val
                if vol > v_rucsac:
                    self._fitness_value = 0
                else:
                    self._fitness_value = rez

        return self._fitness_value

def gen_pop_initiala():
    p = []
    for i in range(pop_size + 1):
        ind = Individ()
        ind.generate_random()
        p.append(ind)
    return p


def selectia_parintilor(): #demonstrativ
    p1 = population[random.randint(0, pop_size - 1)]
    p2 = population[random.randint(0, pop_size - 1)]
    return [p1, p2]

def recombinare(parinti):
    p1, p2 = parinti
    pct_taiere = random.randint(1, len(obiecte)-2) 
    c1 = Individ(p1.get_cromozom()[:pct_taiere] + p2.get_cromozom()[pct_taiere:])
    c2 = Individ(p1.get_cromozom()[pct_taiere:] + p2.get_cromozom()[:pct_taiere])
    return [c1, c2]

def mutatie(temp_gen):
    #se aplica pe fiecare ind in parte
    for i in range(pop_size-1):
        ind = temp_gen[i]
        ind.mutate()   
    return temp_gen    

def med_fitness_per_populatie(pop):
    total = 0
    for ind in pop:
        total += ind.get_fitness() #facem pe obiect, daca fac Individ cu clasa ar fi metoda statica
    return total / pop_size

#executia alg

population = gen_pop_initiala()

print(med_fitness_per_populatie(population))

for i in range(nr_gen):
    print(f'gen: {i}')
    print(med_fitness_per_populatie(population))
    temp_gen = []
    for j in range(int((pop_size + 1) / 2)):
        parinti = selectia_parintilor()
        copii = recombinare(parinti)
        temp_gen.extend(copii)
    temp_gen = mutatie(temp_gen)

    population.extend(temp_gen)
    population= sorted(population, key=lambda ind: ind.get_fitness(), reverse=True)[:pop_size] #sortam populatia dupa fitness si luam doar primii pop_size indivizi
    population = population[:pop_size] #ii luam pe primii 10

print('populatie finala:')

print(med_fitness_per_populatie(population))

for ind in population:
    ind.print_cromozom()
    print(ind.get_fitness())






