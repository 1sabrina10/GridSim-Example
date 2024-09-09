.un calcul distribué.
.La solution proposée repose sur un système de grille informatique simulé à l'aide de la bibliothèque GridSim.
.Cette solution permet de distribuer les calculs de multiplication de matrices sur plusieurs ressources disponibles dans la grille, en
assignant des tâches de calcul (gridlets) à des machines virtuelles.
.modèle simple où un utilisateur envoie des gridlets à des ressources disponibles pour le calcul.
.Les entités principales de la grille sont les utilisateurs (GridUsers) et les ressources (GridResources).
.Les utilisateurs envoient des tâches de calcul aux ressources, qui les exécutent et renvoient les résultats.
