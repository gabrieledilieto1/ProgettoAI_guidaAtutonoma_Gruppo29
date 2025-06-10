
package scr;


public class Sample {
     double[] features;
    int cls;


    public Sample(double[] features, int cls) {
        this.features = features; //inizializzazione delle feature
        this.cls = cls;				//classe
    }

    public Sample(double[] features) {
        this.features = features;
        this.cls = -1; 
    }

    
    public Sample(String line) {
        String[] parts = line.split(";");
        int n = parts.length;
        features = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            features[i] = Double.parseDouble(parts[i].trim());
        }
        this.cls = Integer.parseInt(parts[n - 1].trim());
    }

    
    //distanza euclidea per trovare il punto più vicino
    public double distance(Sample other) {
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += Math.pow(this.features[i] - other.features[i], 2);
        }
        return Math.sqrt(sum);
    }
}
