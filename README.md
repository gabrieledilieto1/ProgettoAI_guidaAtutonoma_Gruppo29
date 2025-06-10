# Progetto AI - Guida Autonoma con Nearest Neighbor su TORCS

**Gruppo 29**  
Componenti:  
- Cocco Luigi  
- D'onofrio Matteo  
- Di Lieto Gabriele  
- Ciniello Lorenzo  

## 🏁 Obiettivo del Progetto

Il progetto consiste nello sviluppo di un sistema di guida autonoma per il simulatore **TORCS** (The Open Racing Car Simulator).  
L'auto è allenata su un dataset creato durante le sessioni di guida umana sul tracciato **"road tracks / FORZA"**, e prende decisioni utilizzando un approccio di tipo **Nearest Neighbor**.

## 📁 Struttura dei File

La repository contiene i seguenti file principali:

- **Client.java** – Punto d’ingresso del programma, gestisce la connessione con il simulatore.
- **Controller.java** – Interfaccia che definisce i metodi per il controllo del veicolo.
- **SimpleDriver.java** – Driver base per test iniziali.
- **DeadSimpleSoloController.java** – Controllore semplice usato per la raccolta dati.
- **NearestNeighbor.java** – Implementazione del classificatore NN per la scelta dell'azione.
- **Sample.java** – Classe che rappresenta un singolo esempio del dataset.
- **SensorModel.java** / **MessageBasedSensorModel.java** – Modellano i sensori ricevuti da TORCS.
- **Action.java** – Rappresenta un’azione che l’auto può compiere.
- **SocketHandler.java**, **MessageParser.java**, **CharReader.java** – Gestione della comunicazione socket e parsing dei messaggi da TORCS.

## 📊 Dataset

Il file `dataset.csv` contiene i seguenti feature per ciascun esempio:
- `Speed`  
- `DistanzaLineaCentrale`  
- `SensoreSX1`, `SensoreSX2`  
- `SensoreCentrale`  
- `SensoreDX1`, `SensoreDX2`  
- `Angolo`  
- `Classe` (azione da eseguire)

Questi dati vengono confrontati in fase di inferenza per determinare l'azione più vicina.

## 🧠 Metodo di Apprendimento

Utilizziamo un semplice algoritmo di **Nearest Neighbor**:  
per ogni stato attuale dell'auto (vettore di feature), il sistema seleziona l'azione associata al campione più vicino nel dataset (in termini di distanza euclidea).

## 🚗 Gara Finale

- **Percorso**: `road tracks / FORZA`
- **Condizioni**: guida completamente autonoma basata solo sui dati di input dei sensori e lo storico raccolto.
- **Obiettivo**: completare il tracciato minimizzando gli errori e mantenendo una guida stabile e fluida.

## ▶️ Come Eseguire

1. Avviare TORCS in modalità client server.
2. Compilare i file Java:
   ```bash
   javac *.java
