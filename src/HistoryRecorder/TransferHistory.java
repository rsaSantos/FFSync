package HistoryRecorder;

import java.io.*;
import java.util.*;
import Logs.LogsRecord;
import Logs.TransferLogs;

///This class is will show all the transfers of a synchronization.
/**
 * This class will store all the transfers of a synchronization, using the class \ref FileTransferHistory. \n
 * It will be saved and loaded on a file. \n
 * This allows the main system to write the information, so the html page can use it later.
 */
public class TransferHistory implements Serializable {

    /// The storage of all the transfers of a synchronization.
    private final Map<String,FileTransferHistory> transfers;

    /// Basic Constructor.
    public TransferHistory(){
        transfers = new HashMap<>();
    }

    /**
     * This constructor will load the class from a previously saved file.
     * The file was previously saved whit the method \ref saveTransferHistory.
     *
     * @param filepath The filepath where the information is stored.
     * @throws IOException IO Exception.
     * @throws ClassNotFoundException Class Exception.
     */
    public TransferHistory(String filepath) throws IOException, ClassNotFoundException {
        ObjectInputStream is =
                new ObjectInputStream(new FileInputStream(filepath));
        TransferHistory transferHistory = (TransferHistory) is.readObject();

        this.transfers = new HashMap<>();
        for (Map.Entry<String, FileTransferHistory> file : transferHistory.transfers.entrySet()) {
            this.transfers.put(file.getKey(), new FileTransferHistory(file.getValue()));
        }
        is.close();
    }

    /**
     * This method will update all the information in \ref transfers from the given logs. \n
     * Will probably be the logs stored in the \ref LogsManager.
     *
     * @param logs The logs to update the information.
     */
    public void updateLogs(Map<String, LogsRecord> logs){
        List<String> remove = new ArrayList<>();

        for (Map.Entry<String, FileTransferHistory> file : transfers.entrySet()){
            LogsRecord fileTransferHistory;
            if( (fileTransferHistory = logs.remove(file.getKey())) != null) {
                long time = fileTransferHistory.fileTime().toMillis();
                if (time > file.getValue().getLastUpdated()) file.getValue().setLastUpdated(time);
            }
            else remove.add(file.getKey());
        }

        for(String file : remove)
            transfers.remove(file);

        for (Map.Entry<String, LogsRecord>file : logs.entrySet())
            transfers.put(file.getKey(),new FileTransferHistory(file.getValue().fileTime(), -1,-1));
    }

    /**
     * This method will update all the information in \ref transfers from the given transfersSet. \n
     * Will probably be generated by the \ref TransferHandler.
     *
     * @param transfersSet the set of all the transfers.
     */
    public void updateGuide(Set<TransferLogs> transfersSet){
        for(TransferLogs fileTransfer:transfersSet) {
            FileTransferHistory fileTransferHistory;
            if ( (fileTransferHistory = this.transfers.get(fileTransfer.fileName())) != null) {
                fileTransferHistory.setTimeOfTransfer(fileTransfer.elapsedTime());
                fileTransferHistory.setBitsPSeg(fileTransfer.bitsPSeg());
            }
        }
    }

    /// Stores the class in a given a filepath.
    public void saveTransferHistory(String filepath) throws IOException {
        ObjectOutputStream os =
                new ObjectOutputStream(new FileOutputStream(filepath));

        os.writeObject(this);
        os.flush();
        os.close();
    }

    /**
     * This simple method is a simple toString method but modified for html \n
     * Will join the information given by every \ref FileTransferHistory.
     *
     * @return The string that has the information with the format the html needs.
     */
    public String toHTML(){

        StringBuilder html = new StringBuilder();

        for(HashMap.Entry<String,FileTransferHistory> entry: transfers.entrySet()) {
            html.append(entry.getValue().toHTML(entry.getKey()));
        }
        return html.toString();
    }

}
