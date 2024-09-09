import java.util.*;
import gridsim.*;

class MatrixMultiplicationExample extends GridSim {
    private Integer ID_; // Identifiant de l'utilisateur
    private String name_; // Nom de l'utilisateur
    private GridletList list_; // Liste des calculs à effectuer
    private GridletList receiveList_; // Liste des résultats reçus
    private int totalResource_; // Nombre total de ressources disponibles

    MatrixMultiplicationExample(String name, double baud_rate, int total_resource)
            throws Exception {
        super(name, baud_rate); // Appel du constructeur de la classe mère GridSim
        this.name_ = name;
        this.totalResource_ = total_resource;
        this.receiveList_ = new GridletList(); // Initialisation de la liste des résultats

        this.ID_ = new Integer(getEntityId(name)); // Obtention de l'ID de l'entité (utilisateur)
        System.out.println("Creating a grid user entity with name = " +
                name + ", and id = " + this.ID_);

        this.list_ = createGridlet(this.ID_.intValue()); // Création des calculs à effectuer
        System.out.println(name + ": Creating " + this.list_.size() +
                " Gridlets");
    }

    public void body() {
        int resourceID[] = new int[this.totalResource_]; // Tableau pour stocker les IDs des ressources
        double resourceCost[] = new double[this.totalResource_]; // Tableau pour stocker les coûts des ressources
        String resourceName[] = new String[this.totalResource_]; // Tableau pour stocker les noms des ressources

        LinkedList resList; // Liste pour stocker les ressources disponibles
        ResourceCharacteristics resChar; // Caractéristiques des ressources

        while (true) {
            super.gridSimHold(1.0); // Attente d'une unité de temps
            resList = super.getGridResourceList(); // Récupération de la liste des ressources
            if (resList.size() == this.totalResource_) // Vérification si le nombre de ressources est suffisant
                break;
            else {
                System.out.println(this.name_ +
                        ": Waiting to get list of resources ..."); // Attendre la disponibilité des ressources
            }
        }

        int i = 0;
        for (i = 0; i < this.totalResource_; i++) {
            resourceID[i] = ((Integer) resList.get(i)).intValue(); // Récupération de l'ID de chaque ressource
            super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
                    GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_); // Envoi de la demande des caractéristiques de la ressource
            resChar = (ResourceCharacteristics) super.receiveEventObject(); // Réception des caractéristiques de la ressource
            resourceName[i] = resChar.getResourceName(); // Récupération du nom de la ressource
            resourceCost[i] = resChar.getCostPerSec(); // Récupération du coût de la ressource par unité de temps

            System.out.println(this.name_ +
                    ": Received ResourceCharacteristics from " +
                    resourceName[i] + ", with id = " + resourceID[i]); // Affichage des informations de la ressource reçue

            super.recordStatistics("\"Received ResourceCharacteristics " +
                    "from " + resourceName[i] + "\"", ""); // Enregistrement des statistiques
        }

        Gridlet gridlet;
        String info;

        int id = 0;
        int[][] matrixA = {{1, 1, 3}, {1, 2, 1}, {4, 3, 6}}; // Première matrice
        int[][] matrixB = {{4, 5, 2}, {6, 5, 4}, {3, 2, 1}}; // Deuxième matrice

        for (i = 0; i < this.list_.size(); i++) {
            gridlet = (Gridlet) this.list_.get(i); // Récupération d'un calcul à effectuer
            info = "Gridlet_" + gridlet.getGridletID();
            id = GridSimRandom.intSample(this.totalResource_); // Choix aléatoire de la ressource pour effectuer le calcul
            System.out.println(this.name_ + ": Sending " + info + " to " +
                    resourceName[id] + " with id = " + resourceID[id]); // Affichage des informations d'envoi du calcul
            super.gridletSubmit(gridlet, resourceID[id]); // Soumission du calcul à la ressource sélectionnée
            super.recordStatistics("\"Submit " + info + " to " +
                    resourceName[id] + "\"", ""); // Enregistrement des statistiques

            // Calcul de la ligne et de la colonne de la cellule résultante
            int row = i / matrixB[0].length; // Calcul de l'indice de ligne en fonction du nombre total de lignes dans la matrice B
            int col = i % matrixB[0].length; // Calcul de l'indice de colonne en fonction du nombre total de colonnes dans la matrice B

            // Affichage du résultat de la multiplication de matrices
            System.out.println("Resultat pour la position [" + row + "][" + col + "]: " + gridlet.getGridletLength());

            gridlet = super.gridletReceive(); // Réception du résultat du calcul
            System.out.println(this.name_ + ": Receiving Gridlet " +
                    gridlet.getGridletID()); // Affichage de l'ID du calcul reçu
            super.recordStatistics("\"Received " + info + " from " +
                    resourceName[id] + "\"", gridlet.getProcessingCost()); // Enregistrement des statistiques de réception
            this.receiveList_.add(gridlet); // Ajout du résultat à la liste des résultats reçus
        }

        super.shutdownGridStatisticsEntity(); // Arrêt de l'entité de collecte des statistiques de grille
        super.shutdownUserEntity(); // Arrêt de l'entité utilisateur
        super.terminateIOEntities(); // Arrêt des entités d'entrée/sortie
        System.out.println(this.name_ + ": Exiting body()"); // Affichage de la sortie du corps de l'utilisateur
    }

    public GridletList getGridletList() {
        return this.receiveList_; // Renvoie la liste des résultats reçus
    }

    private GridletList createGridlet(int userID) {
        GridletList list = new GridletList();

        int id = 0;
        // Matrices à multiplier
        int[][] matrixA = {{1, 1, 3}, {1, 2, 1}, {4, 3, 6}};
        int[][] matrixB = {{4, 5, 2}, {6, 5, 4}, {3, 2, 1}};

        // Calcul de chaque valeur de la matrice résultante
        for (int row = 0; row < matrixA.length; row++) {
            for (int col = 0; col < matrixB[0].length; col++) {
                // Initialisation de la valeur de la cellule de la matrice résultante
                int value = 0;
                // Calcul de la valeur de la cellule en multipliant et additionnant les produits des éléments correspondants
                for (int i = 0; i < matrixA[row].length; i++) {
                    value += matrixA[row][i] * matrixB[i][col];
                }
                // Création d'un calcul pour représenter le résultat
                Gridlet gridlet = new Gridlet(id++, value, 1000, 1000);
                gridlet.setUserID(userID); // Attribution de l'ID de l'utilisateur au calcul
                list.add(gridlet); // Ajout du calcul à la liste
            }
        }

        return list; // Renvoie la liste des calculs créés
    }

    public static void main(String[] args) {
        System.out.println("Starting Matrix Multiplication Example");

        try {
            int num_user = 1; // Nombre d'utilisateurs (ici, un seul utilisateur)
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            String[] exclude_from_file = {""};
            String[] exclude_from_processing = {""};
            String report_name = null;

            GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
                    exclude_from_processing, report_name); // Initialisation de GridSim

            int total_resource = 3; // Nombre total de ressources (modifier selon vos besoins)
            for (int i = 0; i < total_resource; i++) {
                createGridResource("Resource_" + i); // Création des ressources
            }

            MatrixMultiplicationExample user = new MatrixMultiplicationExample("User_0", 560.00, total_resource); // Création de l'utilisateur

            GridSim.startGridSimulation(); // Démarrage de la simulation de grille

            GridletList newList = null;
            newList = user.getGridletList();
            printGridletList(newList, "User_0"); // Affichage des résultats de l'utilisateur

            System.out.println("Finish Matrix Multiplication Example"); // Fin de l'exemple
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
    }

    private static GridResource createGridResource(String name) {
        MachineList mList = new MachineList();
        int mipsRating = 377;
        mList.add(new Machine(0, 4, mipsRating));

        String arch = "Sun Ultra";
        String os = "Solaris";
        double time_zone = 9.0;
        double cost = 3.0;

        ResourceCharacteristics resConfig = new ResourceCharacteristics(
                arch, os, mList, ResourceCharacteristics.TIME_SHARED,
                time_zone, cost);

        double baud_rate = 100.0;
        long seed = 11L * 13 * 17 * 19 * 23 + 1;
        double peakLoad = 0.0;
        double offPeakLoad = 0.0;
        double holidayLoad = 0.0;
        LinkedList Weekends = new LinkedList();
        Weekends.add(new Integer(Calendar.SATURDAY));
        Weekends.add(new Integer(Calendar.SUNDAY));
        LinkedList Holidays = new LinkedList();
        GridResource gridRes = null;
        try {
            gridRes = new GridResource(name, baud_rate, seed,
                    resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends,
                    Holidays); // Création de la ressource
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Creates one Grid resource with name = " + name); // Affichage de la création de la ressource
        return gridRes; // Renvoie la ressource créée
    }

    private static void printGridletList(GridletList list, String name) {
        int size = list.size();
        Gridlet gridlet;

        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT for " + name + " ==========");
        System.out.println("Gridlet ID" + indent + "STATUS" + indent +
                "Resource ID" + indent + "Cost");

        for (int i = 0; i < size; i++) {
            gridlet = (Gridlet) list.get(i);
            System.out.print(indent + gridlet.getGridletID() + indent
                    + indent);

            if (gridlet.getGridletStatus() == Gridlet.SUCCESS)
                System.out.print("SUCCESS");

            System.out.println(indent + indent + gridlet.getResourceID() +
                    indent + indent + gridlet.getProcessingCost());
        }
    }
}