# STUDIO PARADIGM Project

## 🔒 Configurazione del Database (Importante)

Questo progetto utilizza il *Resource Filtering* di Maven per iniettare dinamicamente le credenziali nel file `persistence.xml`. **Se non esegui questo passaggio, l'applicazione non riuscirà a connettersi al database.**

### 1. Crea il file delle credenziali
Nella **cartella radice (root)** del progetto (allo stesso livello del file `pom.xml`), crea un nuovo file di testo e chiamalo esattamente:
`secrets.properties`

### 2. Inserisci i tuoi dati locali
Apri il file appena creato e incolla le seguenti righe, modificando i valori con quelli del tuo database locale (creare un nuovo schema di nome:db_progetto_studio_paradigm)

```properties
# Sostituisci i valori con le tue credenziali locali
db.url=jdbc:mysql://127.0.0.1:3306/db_progetto_studio_paradigm
db.user=tuo_username
db.password=tua_password
```

### 3. Creare il file url.properties in src/main/resources
Apri il file `url.properties` e inserisci la seguente riga:

```properties
# Sostituisci i valori il tuo percorso dove vuoi salvare i file di materiale didattico
upload.dir=C:/il tuo percorso

```
