package scr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighbor {
    
    private List<Sample> trainingData;
    private String firstLineOfTheFile;

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.firstLineOfTheFile = "Speed; DistanzaLineaCentrale; SensoreSX1; SensoreSX2; SensoreCentrale; SensoreDX1; SensoreDX2; Angolo; Classe";
        this.readPointsFromCSV(filename);
    }

    private void readPointsFromCSV(String filename) {
        try {	//lettura con buffered reader
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(firstLineOfTheFile)) {
                    continue; // Serve a non leggere l'intestazione
                }
                
                trainingData.add(new Sample(line));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int classify(Sample targetPoint){
        if (trainingData.isEmpty()) {	//controlla se il training set è presente
            System.out.println("training set vuoto");

           return -1; 
       }

       Sample nearestNeighbor = trainingData.get(0); 
       double minDistance = targetPoint.distance(nearestNeighbor); 


//in questo modo andiamo a cercare il punto più vicino     
       for (Sample point : trainingData) {
           double distance = targetPoint.distance(point);
           if (distance < minDistance) {
               minDistance = distance;
               nearestNeighbor = point;
           }
       }

       return nearestNeighbor.cls;
    }

    public List<Sample> getTrainingData() {
        return trainingData;
    }
}
