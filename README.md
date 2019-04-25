# Projet Mini Serveur Web by P@p!H@ck3R (M.B.C.M)  

Ceci est un mini serveur web codé en Java. C'est un serveur web assez basique n'acceptant que des requêtes dont la méthode est de type `GET`.  
Par défaut, il tourne sur le port `4000` (si ce dernier n'est pas spécifié dans la commande avec l'option `-p` ou `--port`) et dispose d'un mode verbeux (option `-v` ou `--verbose`) qui peut être à `true` ou `false`. Ce mode verbeux permet d'afficher plus d'informations du serveur au niveau de la console (`mode debug` en quelque sorte).  
Le repertoire `htdocs` représente le repertoire de publication ou racine du serveur et contient donc l'ensemble des ressources dont l'utilisateur demandera. C'est l'équivalent du dossier `www` au niveau de `WAMP SERVER` ou encore `htdocs` avec `XAMPP`.  

# Utilisation  
La classe `MiniServer.java` contient le code métier du serveur et `RunMiniServer.java` constitue le point d'entrée du programme. Cette dernière permet de lancer le serveur.  

## Commandes  

* `java RunMiniServer` lance le serveur sur le port 4000 (port par défaut), le mode verbeux est à `true` par défaut.  

* `java RunMiniServer -h || java RunMiniServer --help`, permet d'obtenir l'aide de la commande.  

* `java RunMiniServer -p, --port <portDecoute>`, l'option `-p` ou `--port` permet de spécifier le port à écouter.  

* `java RunMiniServer -v, --verbose <modeVerbeux>`, l'option `-v` ou `--verbose` permet d'activer (`true`) ou de désactiver (`false`) le mode verbeux. Le serveur démarrera sur le port par défaut (4000).  

* `java RunMiniServer -p <portDecoute> -v <modeVerbeux> || java RunMiniServer --port <portDecoute> --verbose <modeVerbeux>`, permet de lancer le serveur sur le port `<portDecoute>` avec le mode verbeux à `<modeVerbeux>`.  

## Exécution de Python  

Après la mise en place des fonctionnalités obligatoires du projet, j'ai doté le serveur des fonctionnalités optionnelles telles que :  
* Le listing des répertoires avec des liens hypertextes  
* L'affichage du résultat de l'interpretation d'un fichier python (`.py`) lorsqu'une telle ressource est demandée  

Pour cette dernière fonctionnalité, j'ai utilisé la libraire `Jython` qui fournit une API plutôt pas mal pour l'éxécution de code `Python` dans un programme `Java`. Le repertoire `lib` du projet contient le `jar` représentant la bibliothèque. Cependant, il faut veiller à l'inclure dans votre `path` lors de la compilation et/ou du démarrage du serveur.  
Vous pouvez procéder comme suit :  

* Compilation:  
        `javac -cp lib/jython-standalone-2.7.0.jar:. RunMiniServer.java`  

* Exécution:  
`java -cp lib/jython-standalone-2.7.0.jar:. RunMiniServer.java [-p | --port <port>] [-v | --verbose <verbose> [-h | --help]`
