import random

#Ești un șofer de livrări și pleci din depozitul din Iași.
# Trebuie să ajungi în celelalte 4 orașe — Bacău, Suceava, Botoșani și Vaslui — fiecare o singură dată, iar la final să te întorci înapoi în Iași.

# Parametrii problemei (ordinea orașelor potrivită exact pe matricea ta)
orase = ["Iași", "Vaslui", "Bacău", "Suceava", "Botoșani"]
matrice_distante = [
    [0,   70, 130, 140, 120],
    [70,   0,  85, 180, 185],
    [130, 60,   0, 145, 190],
    [140, 170, 145,  0,  45],
    [120, 185, 190, 45,   0]
]

punct_start = 0  # Iași

# Parametrii algoritmului
dim_populatie = 5
nr_generatii = 10
probabilitate_mutatie = 5  # 5%

def genereaza_individ():
    individ = list(range(len(orase)))
    random.shuffle(individ)
    return individ

def generatie_initiala():
    return [genereaza_individ() for _ in range(dim_populatie)]

def fitness(individ):
    distanta_totala = 0
    for i in range(len(individ)):
        oras_curent = individ[i]
        oras_urmator = individ[(i + 1) % len(individ)]
        distanta_totala += matrice_distante[oras_curent][oras_urmator]
    return distanta_totala

def selectie_parinti(populatie):
    scoruri = [fitness(individ) for individ in populatie]
    calitati = [1 / scor for scor in scoruri]
    suma_calitati = sum(calitati)
    parinti = []
    for _ in range(2):
        r = random.uniform(0, suma_calitati)
        suma_partial = 0
        for i, calitate in enumerate(calitati):
            suma_partial += calitate
            if suma_partial >= r:
                parinti.append(populatie[i])
                break
    return parinti

def recombinare(parinti):
    p1, p2 = parinti
    lungime = len(p1)

    a, b = sorted(random.sample(range(lungime), 2))
    segment_p1 = p1[a:b]
    rest_p2 = [client for client in p2 if client not in segment_p1]
    c1 = rest_p2[:a] + segment_p1 + rest_p2[a:]

    segment_p2 = p2[a:b]
    rest_p1 = [client for client in p1 if client not in segment_p2]
    c2 = rest_p1[:a] + segment_p2 + rest_p1[a:]
    return c1, c2

def mutatie(individ):
    if random.randint(1, 100) <= probabilitate_mutatie:
        idx1, idx2 = random.sample(range(len(individ)), 2)
        individ[idx1], individ[idx2] = individ[idx2], individ[idx1]
    return individ

def formateaza_traseu(individ):
    # Rotim lista astfel încât afișarea să înceapă mereu din Iași (index 0)
    idx_start = individ.index(punct_start)
    traseu_rotit = individ[idx_start:] + individ[:idx_start] + [punct_start]
    return " -> ".join(orase[i] for i in traseu_rotit)

# --- BUCLA PRINCIPALĂ (AICI SE ÎNTÂMPLĂ EVOLUȚIA) ---
populatie = generatie_initiala()
cel_mai_bun_global = None

for gen in range(1, nr_generatii + 1):
    # 1. Găsim cel mai bun individ din generația curentă (distanța cea mai mică)
    cel_mai_bun_gen = min(populatie, key=fitness)
    cost_curent = fitness(cel_mai_bun_gen)

    # Actualizăm recordul absolut dacă am găsit ceva mai bun
    if cel_mai_bun_global is None or cost_curent < fitness(cel_mai_bun_global):
        cel_mai_bun_global = cel_mai_bun_gen[:]

    # 2. Afișăm progresul generației
    print(f"Generația {gen:2d} | Fitness (distanță): {cost_curent} km | Traseu: {formateaza_traseu(cel_mai_bun_gen)}")

    # 3. Construim generația următoare
    noua_populatie = [cel_mai_bun_gen[:]]  # Elitism: păstrăm cel mai bun individ ca să nu pierdem progresul

    while len(noua_populatie) < dim_populatie:
        parinti = selectie_parinti(populatie)
        c1, c2 = recombinare(parinti)
        noua_populatie.append(mutatie(c1))
        if len(noua_populatie) < dim_populatie:
            noua_populatie.append(mutatie(c2))

    populatie = noua_populatie

print("\n--- REZULTAT FINAL ---")
print(f"Cel mai bun traseu găsit: {formateaza_traseu(cel_mai_bun_global)}")
print(f"Distanță minimă: {fitness(cel_mai_bun_global)} km")