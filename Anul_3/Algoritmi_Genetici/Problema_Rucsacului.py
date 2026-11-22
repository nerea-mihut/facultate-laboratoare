#algoritm GA pt problema rucsacului

import random

##setarile/parametrii problemei
v_rucsac = 35
    #volum , valoare
obiecte = [
    (15,5),
    (1,6),
    (2,2),
    (5,10),
    (2,15),
    (20,20),
    (12,6)
]

#setarile algoritmului
def afisare_valoare_generatie(population):
    with open("rezultat.csv", "a") as f:
        for i in range(len(population)):
            f.write(f"generatia: {i}\n")
            ind = population[i]
            val = 0
            vol = 0
            for j in range(len(obiecte)):
                if ind[j] == 1:
                    vol += obiecte[j][0]
                    val += obiecte[j][1]
            f.write(f"individ: {ind} valoare: {val} volum: {vol}\n")


pop_size = 10
nr_gen = 101
prob_mut = 5 #5% probabilitate mucatie

#reprezentarea individului
#in cadrul acestei probleme, individul este un array de buleeni

def generate_init_population():
    p = []
    for i in range(pop_size+1):
        ind = [random.randint(0,1) for i in range(len(obiecte))] #genereaza pt fiecare obiect o valoare de 0 sau 1
        p.append(ind)
    return p

#functie fittnessA
def fitness(individ):
    # rez = suma de valori
    rez = 0
    vol = 0
    for i in range(len(obiecte)):
        if individ[i]==1:
            rez += obiecte[i][1]    #1 este valoarea obiectului
            vol += obiecte[i][0]    #0 este volumul obiectului
    if vol > v_rucsac:
        return 0
    return rez

def selectia_parintilor():  # Demonstrativ! De modificat ulterior
    p1 = population[random.randint(0, pop_size-1)] #luam random 2 parinti, nu poate fi asa, ramane momentan
    p2 = population[random.randint(0, pop_size-1)]
    return [p1,p2]

def recombinare(parinti):
    p1,p2 = parinti
    pct_taiere = random.randint(1, len(obiecte)-2)
    c1 = p1[:pct_taiere] + p2[pct_taiere:]
    c2 = p2[:pct_taiere] + p1[pct_taiere:]
    return [c1 ,c2]

def mutatie(temp_gen):
#teoretic: exista 3 tipuri de mutatie: buna, rea, neutra
    for i in range(pop_size-1):
        ind = temp_gen[i]
        for j in range(len(obiecte)-1):
            has_mutation = random.randint(0,100) <= prob_mut
            if (has_mutation):
                ind[j] = 0 if ind[j]==1 else 1 #neaga valoarea, nu am stiut cum se face negarea bittilor
    return temp_gen


def supravietuire(temp_gen):
    return temp_gen

def med_fitness_per_population(pop):
    total = 0
    for ind in pop:
        total += fitness(ind)
    return total / pop_size

# Executia algoritmului
population = generate_init_population()
print(med_fitness_per_population(population))

for i in range(nr_gen):
    print(f"generatia: {i}")
    print(med_fitness_per_population(population))
    temp_gen = []
    for j in range(pop_size+1):
        parinti = selectia_parintilor()
        copii = recombinare(parinti)
        temp_gen.extend(copii)          #past: append(copii) -> adauga lista de copii; .extend(copii) -> adauga continutul listei de copii 
    temp_gen = mutatie(temp_gen)
    population.extend(temp_gen)
    population = sorted(population, key=lambda ind: fitness(ind), reverse=True)
    population = population[:pop_size]  # supravietuiesc cei mai buni


print("populatie finala")
print(med_fitness_per_population(population))
for ind in population:
    print(ind)
    print(med_fitness_per_population(population))