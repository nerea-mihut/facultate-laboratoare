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


def gen_pop_initiala():
    p = []
    for i in range(pop_size + 1):
        ind = [random.randint(0,1) for i in range(len(obiecte))] #list comprenhasion
        p.append(ind)
        print(ind)
    return p



def selectia_parintilor(): #nu poate fi random, acm am facut ca ex asa sa fie
    p1 = population[random.randint(0, pop_size - 1)]
    p2 = population[random.randint(0, pop_size - 1)]
    return [p1, p2]

def recombinare(parinti):
    p1, p2 = parinti
    pct_taiere = random.randint(1, len(obiecte)-2) 
    c1 = p1[:pct_taiere] + p2[pct_taiere:]
    c2= p1[pct_taiere:] + p2[:pct_taiere]
    return [c1, c2]

def mutatie(temp_gen): #asta nu-i bine, sa nu-l luam ca exemplu ca nu trb sa fie for in for la generatia temporara ci in gen actuala
    #se aplica pe fiecare ind in parte
    for i in range(pop_size-1):
        ind = temp_gen[i]
        for j in range(len(obiecte) - 1):
            has_mutation = random.randint(0, 100) <= probabilitate_mutatie
            if (has_mutation):
                val = ind[j]
                ind[j] = 0 if val == 1 else 1 #facem un swap, like daca are mutatie schimbam nr ca s afie modificat
    return temp_gen

def fitness(ind):
    #rez = suma de valori
    rez = 0
    vol = 0
    for i in range(len(obiecte)):
        if ind[i] == 1:
            rez += obiecte[i][1] #1 este val obiectului 
            vol += obiecte[i][0] #0 este vol obiectului pt ca obiectu ii rep de un tuplu (vol, val) deci 0 ii vol si 1 ii val
    if vol > v_rucsac:
        return 0
    return rez

def med_fitness_per_populatie(pop):
    total = 0
    for ind in pop:
        total += fitness(ind)
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
    population= sorted(population, key=lambda ind: fitness(ind), reverse=True)[:pop_size] #sortam populatia dupa fitness si luam doar primii pop_size indivizi
    population = population[:pop_size] #ii luam pe primii 10



print('populatie finala:')

print(med_fitness_per_populatie(population))

for ind in population:
    print(ind)
    print(fitness(ind))






