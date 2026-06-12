# Progetto-IdS
Progetto di Ingegneria del Software, docente Domenico Amalfitano
# 🚀 Piattaforma di Gestione e Condivisione di Materiale Didattico 

> **Progetto di Ingegneria del Software** > Anno Accademico: [2025/2026] - Università: Università degli Studi di Napoli Federico II

**StudioParadigm** è un sistema software realizzato al fine di facilitare la gestione e la condivisione di materiale didattico 
pubblicato dai docenti e consultato dagli studenti. Il sistema dovrà consentire ai docenti di organizzare e rendere disponibili 
diversi tipi di contenuti relativi ai propri insegnamenti, e agli studenti di accedere al materiale dei corsi a cui 
risultano iscritti.	

---

## 👥 Il Team

* **Chiara Carotenuto** - 
* **Sara Aquino** - 
* **Francesco Caputo** - 

---

## 🛠️ Tecnologie e Strumenti Utilizzati

* **Linguaggio di Programmazione:** [Java]
* **Database:** [ MySQL]
* **Modellazione Architettura (UML):** Visual Paradigm
* **Versionamento:**  [GitHub]

---

## 📐 Architettura e Design (UML)

Tutti i diagrammi UML (Casi d'Uso, Classi, Sequenza, ecc.) non si trovano in questo repository per evitare conflitti sui file binari, ma sono gestiti in cloud. Sulla repository verra pubblicato solamente il progetto finale completato

* Il progetto UML ufficiale è ospitato su **VPository (Visual Paradigm Online)**.

---

## 🤝 Regole di Sviluppo (Workflow del Team)

Per mantenere il progetto pulito e lavorare senza conflitti, seguiamo queste due semplici regole:

### 1. Gestione del Codice (Git)
* Assicurati di essere sul branch corretto prima di lavorare.
* Fai un `git pull` prima di iniziare a scrivere nuovo codice.
* Fai commit piccoli, frequenti e con messaggi chiari (es. `Aggiunta la classe UtenteController`).

### 2. Gestione dei Diagrammi (Visual Paradigm)
* Apri Visual Paradigm Desktop.
* Prima di toccare qualsiasi diagramma, vai su **Team > Update**.
* Fai le tue modifiche al design.
* Prima di chiudere il programma, vai su **Team > Commit** inserendo un breve messaggio di log.

---

##  🔧  💻  Installazione e Avvio Locale

Istruzioni per scaricare ed eseguire il progetto sul proprio computer:

### 📥 **Clona la repository:**
   git clone https://github.com/Chiara004/Progetto-IdS.git


### 📐 **Configurazione Visual Paradigm (Teamwork)**
Per la parte di modellazione UML, utilizziamo il cloud di Visual Paradigm (**VPository**) per permettere il lavoro collaborativo.


### ⚙️ **Procedura di Setup iniziale**:
1. **Accettazione Invito:** Controlla la tua email personale e accetta l'invito al Workspace VPository inviato dal Team Leader. Crea un account scegliendo il piano **Free**.
2. **Connessione Desktop:** Apri l'applicazione Visual Paradigm sul tuo computer.
3. **Login:** Vai nel menu superiore sulla scheda **Team** > **Select Repository**. Inserisci le tue credenziali di VP Online.
4. **Download Progetto:** Vai su **Project** > **Open** > **Open from Teamwork...**. Seleziona il progetto del gruppo e clicca su **Open**.


### 🔒 **Configurazione del Database e Avvio in locale**
Per poter avviare in locale il software è necessario installare una JVM (Java Virtual Machine) per l'esecuzione del progetto Java e 
MYSQL per la connessione al database. Per migliorare l'efficienza e la sicurezza dei dati sono necessari i seguenti passaggi di 
configurazione in locale per poter avviare correttamente il sistema.
Questo progetto utilizza il *Resource Filtering* di Maven per iniettare dinamicamente le credenziali nel file `persistence.xml`. **Se non esegui questo passaggio, l'applicazione non riuscirà a connettersi al database.**

**1. Crea il file delle credenziali**
Nella **cartella radice (root)** del progetto (allo stesso livello del file `pom.xml`), crea un nuovo file di testo e chiamalo esattamente:
`secrets.properties`

**2. Inserisci i tuoi dati locali**
Apri il file appena creato e incolla le seguenti righe, modificando i valori con quelli del tuo database locale (creare un nuovo schema di nome:db_progetto_studio_paradigm)

```properties
# Sostituisci i valori con le tue credenziali locali
db.url=jdbc:mysql://127.0.0.1:3306/db_progetto_studio_paradigm
db.user=tuo_username
db.password=tua_password
```

**3. Creare il file url.properties in src/main/resources**
Apri il file `url.properties` e inserisci la seguente riga:

```properties
# Sostituisci i valori il tuo percorso dove vuoi salvare i file di materiale didattico
upload.dir=C:/il tuo percorso
```
**4. Esecuzione**
Esegui il file 'mainApp.java' nel pacakge Eseguibile e goditi lo spettacolo
