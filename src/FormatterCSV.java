import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FormatterCSV {
    
    public static void main(String[] args) {
        System.out.println("🧹 FORMATTEUR DE CSV");
        System.out.println("=".repeat(60));
        
       
        nettoyerFichier("resultats_detailles.csv", "resultats_propres.csv");
        nettoyerFichier("resultats_resume.csv", "resume_propre.csv");
        
        
        creerRapportLisible("resultats_propres.csv", "rapport_lisible.txt");
    }
    
    private static void nettoyerFichier(String source, String destination) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(source), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new FileWriter(destination, StandardCharsets.UTF_8))) {
            
            String ligne;
            boolean firstLine = true;
            
            while ((ligne = reader.readLine()) != null) {
              
                ligne = ligne.replace("DÃ©duction", "Déduction")
                            .replace("Strafx@gle", "Stratégie")
                            .replace("Ã", "é")
                            .replace("©", "é");
                
                
                if (ligne.contains(",") && !ligne.contains(";")) {
                    ligne = ligne.replace(",", ";");
                }
                
                writer.println(ligne);
            }
            
            System.out.println("✅ Fichier nettoyé : " + destination);
            
        } catch (IOException e) {
            System.out.println("⚠️ Fichier non trouvé : " + source);
        }
    }
    
    private static void creerRapportLisible(String sourceCSV, String destinationTxt) {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceCSV));
             PrintWriter writer = new PrintWriter(new FileWriter(destinationTxt))) {
            
            writer.println("=".repeat(80));
            writer.println("RAPPORT DE PERFORMANCE - NONOGRAM SOLVER");
            writer.println("=".repeat(80));
            writer.println();
            writer.println("Généré le : " + new Date());
            writer.println();
            
            List<String> lignes = new ArrayList<>();
            String ligne;
            
            while ((ligne = reader.readLine()) != null) {
                lignes.add(ligne);
            }
            
            if (!lignes.isEmpty()) {
                // Analyser les données
                Map<String, List<String>> parStrategie = new HashMap<>();
                
                for (int i = 1; i < lignes.size(); i++) { // Skip header
                    String[] colonnes = lignes.get(i).split(";");
                    if (colonnes.length >= 2) {
                        String strategie = colonnes[0];
                        parStrategie.putIfAbsent(strategie, new ArrayList<>());
                        parStrategie.get(strategie).add(lignes.get(i));
                    }
                }
                
                // Afficher par stratégie
                for (Map.Entry<String, List<String>> entry : parStrategie.entrySet()) {
                    writer.println("\n" + "─".repeat(60));
                    writer.println("STRATÉGIE : " + entry.getKey());
                    writer.println("─".repeat(60));
                    
                    int total = entry.getValue().size();
                    int reussites = 0;
                    long tempsTotal = 0;
                    
                    for (String data : entry.getValue()) {
                        String[] cols = data.split(";");
                        if (cols.length >= 5) {
                            if (cols[3].equals("1") || cols[3].equalsIgnoreCase("OUI")) {
                                reussites++;
                            }
                            try {
                                tempsTotal += Long.parseLong(cols[4]);
                            } catch (NumberFormatException e) {
                                // Ignorer
                            }
                        }
                    }
                    
                    writer.println(String.format("Succès : %d/%d (%.1f%%)", 
                        reussites, total, (reussites * 100.0) / total));
                    writer.println(String.format("Temps moyen : %d ms", 
                        tempsTotal / total));
                    writer.println();
                }
            }
            
            System.out.println("✅ Rapport lisible généré : " + destinationTxt);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}