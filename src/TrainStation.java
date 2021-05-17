import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Application;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class TrainStation extends Application {
    private static final int SEATING_CAPACITY = 42; //declare a global constant variable for seating capacity
    private static int checkedInPassengerCount = 0; //declare a variable to get the checked in passenger count
    private static ArrayList<String> checkedInPassengerSeats = new ArrayList<>(); //declare an array list to add seat numbers of checked in passengers
    private Passenger[] waitingRoom = new Passenger[SEATING_CAPACITY]; //represents waiting room array
    private PassengerQueue trainQueue = new PassengerQueue(SEATING_CAPACITY/2); //represents train queue
    private ArrayList<Passenger> trainCompartment = new ArrayList<>(); //represents train compartment

    public static void main (String[] args) {
        Application.launch();
    }

    public void start(Stage primaryStage) throws IOException {
        HashMap<String,String> customerDetails = new HashMap<>(); //hash map key:customer id value:customer name
        HashMap<String,List<String>> bookedSeatsDetails = new HashMap<>(); //hash map key:seat reference number value:array list[date path reference,from,to,customer id,seat number]
        loadPassengerData(customerDetails,bookedSeatsDetails); //call loadPassengerData() method to load booked seats details
        while (customerDetails.size() == 0 || bookedSeatsDetails.size() == 0)  { //check whether any seat reservation details has loaded to the data structure or not
            System.out.println("No booking details has been loaded. Therefore program cannot continue the passenger boarding process.");
            Scanner input = new Scanner(System.in);
            System.out.println("Do you want to quit program? (Y/N) : ");
            String response = input.nextLine().toUpperCase();
            while (!response.equals("Y") && !response.equals("N")) {
                System.out.println("Please enter either 'Y' or 'N' as your answer : ");
                response = input.nextLine().toUpperCase();
            }
            if (response.equals("Y")) {
                System.out.println("Thank you for using our passenger boarding management program. Now you are exiting from the program.");
                System.exit(0);
            } else {
                loadPassengerData(customerDetails,bookedSeatsDetails);
            }
        }

        loop:
        while (true) { //call appropriate method based on the user input
            switch (viewMenu()) {
                case "W":
                case "w":
                    addPassengerToWaitingRoom(customerDetails,bookedSeatsDetails,waitingRoom); //call addPassengerToWaitingRoom() method
                    break;
                case "A":
                case "a":
                    addPassengerToTrainQueue(waitingRoom,trainQueue,trainCompartment); //call addPassengerToTrainQueue() method
                    break;
                case "V":
                case "v":
                    viewTrainQueue(waitingRoom,trainQueue,trainCompartment); //call viewTrainQueue() method
                    break;
                case "D":
                case "d":
                    deletePassenger(trainQueue); //call deletePassenger() method
                    break;
                case "S":
                case "s":
                    storeDataIntoFile(waitingRoom,trainQueue,trainCompartment); //call storeDataIntoFile() method
                    break;
                case "L":
                case "l":
                    loadDataFromFile(waitingRoom,trainQueue,trainCompartment); //call loadDataFromFile() method
                    break;
                case "R":
                case "r":
                    runSimulationAndProduceReport(trainQueue,trainCompartment); //call runSimulationAndProduceReport() method
                    break;
                case "Q":  //allow user to quit program
                case "q":
                    System.out.println("Thank you for using our passenger boarding management program. Now you are exiting from the program.");
                    break loop;
                default:  //execute if there`s no case match
                    System.out.println("Sorry! You have entered an invalid input. Please try again");
            }
        }
    }

    public static void loadPassengerData(HashMap<String,String> customerDetailsList, HashMap<String, List<String>> seatDetailsList) { //let user to load all the seat booking details from database for a specific date and path
        List<String> customerJourneyData = new ArrayList<>();

        GridPane gridlayout1 = new GridPane(); //create GUI to get departure date and path from user
        gridlayout1.setStyle("-fx-background-color:#B17EE0");
        gridlayout1.setPadding(new Insets(20, 20, 20, 20));
        gridlayout1.setVgap(15);
        gridlayout1.setHgap(15);

        Label welcomeMessage = new Label("Welcome to the passenger boarding management program!"); //create labels,buttons and other elements
        welcomeMessage.setFont(new Font("Arial Rounded MT Bold", 24));
        Label description = new Label("Please fill these details in order to continue the passenger boarding process");
        description.setFont(new Font("Arial Rounded MT Bold", 22));
        Label departureDateLabel = new Label("Departure Date: ");
        DatePicker departureDate = new DatePicker();
        departureDate.setDayCellFactory(param -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate currentDate = LocalDate.now();
                setDisable(empty || date.compareTo(currentDate) < 0);
            }
        });
        Label selectPathLabel = new Label("Select path");
        ObservableList<String> pathOptions = FXCollections.observableArrayList("Colombo to Badulla", "Badulla to Colombo");
        ChoiceBox<String> path = new ChoiceBox<>(pathOptions);
        Button OKButton = new Button("   OK   ");

        gridlayout1.add(welcomeMessage, 6, 2, 18, 1); //add created elements into the grid pane
        gridlayout1.add(description, 3, 3, 25, 1);
        gridlayout1.add(departureDateLabel, 6, 7, 3, 1);
        gridlayout1.add(departureDate, 9, 7, 3, 1);
        gridlayout1.add(selectPathLabel, 6, 9, 3, 1);
        gridlayout1.add(path, 9, 9, 3, 1);
        gridlayout1.add(OKButton, 20, 14, 5, 1);

        OKButton.setOnAction(event -> {
            LocalDate date = departureDate.getValue(); //get user inputs and put them into variables
            String pathValue = path.getValue();

            if ((date == null) || (pathValue == null)) { //check whether user have filled all the required data
                Alert emptyDataAlert = new Alert(Alert.AlertType.WARNING);
                emptyDataAlert.setTitle("Fill Data Alert");
                emptyDataAlert.setContentText("Please fill all the required data fields in order to continue the passenger boarding process.");
                emptyDataAlert.showAndWait();
            } else {
                customerJourneyData.add(date.toString());
                customerJourneyData.add(pathValue);

                Stage currentStage = (Stage) OKButton.getScene().getWindow();
                currentStage.close();
            }
        });

        Scene scene1 = new Scene(gridlayout1, 1000, 500);
        Stage primaryStage1 = new Stage();
        primaryStage1.setTitle("Passenger Boarding Management System - Journey Details");
        primaryStage1.setScene(scene1);
        primaryStage1.showAndWait();

        if (customerJourneyData.size() == 2) { //check whether the customer has provided all the required data
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase mongoDatabase = mongoClient.getDatabase("TrainSeatBookingSystem");
            MongoCollection<Document> collection = mongoDatabase.getCollection("CustomerDetails");
            FindIterable<Document> documentData = collection.find();
            for (Document details : documentData) { //get booking data from database to data structure
                if (details.getString("Departure_Date").equals(customerJourneyData.get(0)) && details.getString("Path").equals(customerJourneyData.get(1))) {
                    customerDetailsList.put(details.getString("Customer_NIC"), details.getString("Customer_Name"));
                    ArrayList<String> seatDataList = new ArrayList<>();
                    String datePathReference = details.getString("Departure_Date") + details.getString("Path");
                    seatDataList.add(datePathReference);
                    seatDataList.add(details.getString("From"));
                    seatDataList.add(details.getString("To"));
                    seatDataList.add(details.getString("Customer_NIC"));
                    seatDataList.add(details.getString("Seat_Number"));
                    String seatReference = details.getString("Seat_Number") + datePathReference;
                    seatDetailsList.put(seatReference, seatDataList);
                }
            }
            if (customerDetailsList.size() == 0 || seatDetailsList.size() == 0) {
                System.out.println();
                System.out.println("No seat reservations has been made for provided date and path.");
            } else {
                System.out.println();
                System.out.println("All the booking details related to the provided date and path, has been successfully loaded from the file.");

                GridPane gridlayout2 = new GridPane(); //create GUI to view loaded booking details
                gridlayout2.setPrefWidth(3000);
                gridlayout2.setStyle("-fx-background-color:#22F2E4");
                gridlayout2.setPadding(new Insets(20, 20, 20, 20));
                gridlayout2.setVgap(15);
                gridlayout2.setHgap(15);

                Label message = new Label("These are the loaded booking details related to the provided date and path"); //create labels,buttons and other elements
                message.setFont(new Font("Arial Rounded MT Bold", 24));
                Label nameTitle = new Label("Passenger Name");
                nameTitle.setFont(new Font("Arial Rounded MT Bold", 22));
                Label idTitle = new Label("NIC Number");
                idTitle.setFont(new Font("Arial Rounded MT Bold", 22));
                Label seatTitle = new Label("Seat Number");
                seatTitle.setFont(new Font("Arial Rounded MT Bold", 22));

                int xPosition = 19, yPosition = 7;
                ArrayList<String> seatNumbers = new ArrayList<>();
                for (String seatReference : seatDetailsList.keySet()) {
                    seatNumbers.add(seatDetailsList.get(seatReference).get(4));
                }
                for(int i=0; i<seatNumbers.size(); i++) { //sort seat numbers using bubble sorting algorithm
                    for(int j=0; j<seatNumbers.size()-i-1;j++ ) {
                        if(Integer.parseInt(seatNumbers.get(j)) > Integer.parseInt(seatNumbers.get(j+1))) {
                            String temporaryVariable = seatNumbers.get(j);
                            seatNumbers.set(j,seatNumbers.get(j+1));
                            seatNumbers.set(j+1,temporaryVariable);
                        }
                    }
                }
                for (String seat : seatNumbers) { //create labels to display loaded data
                    for(String seatReference : seatDetailsList.keySet()) {
                        if (seatDetailsList.get(seatReference).get(4).equals(seat)) {
                            Label name = new Label(customerDetailsList.get(seatDetailsList.get(seatReference).get(3)));
                            name.setFont(new Font("Arial Rounded MT Bold", 20));
                            gridlayout2.add(name, xPosition, yPosition, 5, 1);
                            Label id = new Label(seatDetailsList.get(seatReference).get(3));
                            id.setFont(new Font("Arial Rounded MT Bold", 20));
                            gridlayout2.add(id, xPosition + 20, yPosition, 5, 1);
                            Label seatNumber = new Label("Seat " + seatDetailsList.get(seatReference).get(4));
                            seatNumber.setFont(new Font("Arial Rounded MT Bold", 20));
                            gridlayout2.add(seatNumber, xPosition + 40, yPosition, 5, 1);
                            yPosition++;
                        }
                    }
                }

                gridlayout2.add(message, 22, 2, 40, 1);  //add created elements into the grid pane
                gridlayout2.add(nameTitle, 18, 5, 5, 1);
                gridlayout2.add(idTitle, 38, 5, 5, 1);
                gridlayout2.add(seatTitle, 58,5 , 5, 1);

                ScrollPane scrollPane = new ScrollPane(gridlayout2);
                scrollPane.setFitToHeight(true);

                Scene scene2 = new Scene(scrollPane, 3000, 1000);
                Stage primaryStage2 = new Stage();
                primaryStage2.setTitle("Passenger Boarding Management System - Loaded Booking Details");
                primaryStage2.setScene(scene2);
                primaryStage2.showAndWait();
            }
        }
    }

    public static String viewMenu() { //view menu with available options and let user to select an option
        System.out.println();
        System.out.println("Welcome to the passenger boarding management program!");
        System.out.println("From here, you can manage the process of boarding passengers into the A/C compartment of \"Denuwara Menike\" train");
        System.out.println();
        ArrayList<String> menuOptions = new ArrayList<>(Arrays.asList("----------Menu----------", "W - Add a passenger to the waiting room", "A - Add a passenger to the train queue",
                "V - View the train queue", "D - Delete a passenger from the train queue", "S - Store train queue data into a file", "L - Load train queue data back from the file",
                "R - Run the simulation and produce the report", "Q - Quit Program"));
        for(String i : menuOptions) {
            System.out.println(i);
        }
        System.out.println();
        Scanner option = new Scanner(System.in);
        System.out.println("Please enter the relevant letter of the option you want to select : ");
        return option.nextLine(); //return user input
    }

    public static void addPassengerToWaitingRoom(HashMap<String,String> customerDetailsList, HashMap<String, List<String>> seatDetailsList, Passenger[] waitingRoomList) {   //let user to add a passenger to waiting room
        if(checkedInPassengerCount < SEATING_CAPACITY) {
            Scanner input = new Scanner(System.in);
            System.out.println("Please enter the relevant seat number of the passenger in order to add him/her to the waiting room : ");
            String seatNumber = input.nextLine();
            boolean containValue = false;
            for (String seatReference : seatDetailsList.keySet()) {  //check if any reservation has been made for provided seat under selected date and path
                if (seatDetailsList.get(seatReference).get(4).equals(seatNumber)) {
                    containValue = true;
                }
            }
            while ( !containValue || checkedInPassengerSeats.contains(seatNumber)) {
                if(!containValue) {
                    System.out.println("Any reservations hasn`t been made for this seat number for the provided date and path. Please check and re - enter the seat number : ");
                } else {
                    System.out.println("Another passenger has used this seat number before, to check in to the waiting room. Please check and re - enter the seat number : ");
                }
                seatNumber = input.nextLine();
                for (String seatReference : seatDetailsList.keySet()) {
                    if (seatDetailsList.get(seatReference).get(4).equals(seatNumber)) {
                        containValue = true;
                    }
                }
            }
            for (String seatReference : seatDetailsList.keySet()) {  //get the details of the passenger who`s going to be added to waiting room
                if (seatDetailsList.get(seatReference).get(4).equals(seatNumber)) {
                    ArrayList<String> passengerDetails = new ArrayList<>(seatDetailsList.get(seatReference));
                    System.out.println();
                    System.out.println("Following passenger is going to be added to the waiting room");  //show details of the passenger who`s going to be added to waiting room
                    System.out.println("        Passenger Name: " + customerDetailsList.get(passengerDetails.get(3)));
                    System.out.println("        Passenger NIC Number: " + passengerDetails.get(3));
                    System.out.println("        Seat Number: " + passengerDetails.get(4));
                    System.out.println();
                    System.out.println("Do you want to confirm the action? (Y/N): ");  //ask user to confirm adding passenger to waiting room
                    String confirmation = input.nextLine().toUpperCase();

                    while (!confirmation.equals("Y") && !confirmation.equals("N")) {
                        System.out.println("Please enter either 'Y' or 'N' as your answer : ");
                        confirmation = input.nextLine().toUpperCase();
                    }
                    if(confirmation.equals("Y")) {
                        checkedInPassengerSeats.add(seatNumber);
                        int count = 0;
                        for (Passenger passenger : waitingRoomList) {
                            if (passenger != null) {
                                count++;
                            }
                        }
                        waitingRoomList[count] = new Passenger(); //add passenger to waiting room
                        waitingRoomList[count].setName(customerDetailsList.get(passengerDetails.get(3)));
                        waitingRoomList[count].setId(passengerDetails.get(3));
                        waitingRoomList[count].setSeat(passengerDetails.get(4));
                        checkedInPassengerCount++;

                        System.out.println("Following passenger has been added to the waiting room");  //display details of the added passenger
                        System.out.println("        Passenger Name: " + waitingRoomList[count].getName());
                        System.out.println("        Passenger NIC Number: " + waitingRoomList[count].getId());
                        System.out.println("        Seat Number: " + waitingRoomList[count].getSeat());
                    } else {
                        break;
                    }
                }
            }
        }  else {
            System.out.println("All the passengers have been successfully added to the waiting room already. There`s no more passengers left to add to the waiting room");
        }
    }

    public static void addPassengerToTrainQueue(Passenger[] waitingRoomList, PassengerQueue passengerQueue, ArrayList<Passenger> boardedPassengerList) {  //let user to add passengers from waiting room to train queue
        ArrayList<Passenger> passengersToAdd = new ArrayList<>();
        ArrayList<Passenger> addedPassengers = new ArrayList<>();
        Random randomNumber = new Random();
        boolean full = passengerQueue.isFull();  //check whether the queue is full

        if (!full && ((passengerQueue.getQueueArrayElementCount() + boardedPassengerList.size()) != checkedInPassengerCount) && checkedInPassengerCount != 0) {
            int numberOfPassengers = randomNumber.nextInt(6) + 1;  //generate a random number within 1 to 6
            for (int i=0; i<numberOfPassengers; i++) {  //get randomly generated number of passengers from waiting room
                if (waitingRoomList[i] != null) {
                    passengersToAdd.add(waitingRoomList[i]);
                }
            }
            for(int i=0; i<passengersToAdd.size(); i++) { //sort passengers according to their seat number using bubble sorting algorithm
                for(int j=0; j<passengersToAdd.size()-i-1;j++ ) {
                    if(Integer.parseInt(passengersToAdd.get(j).getSeat()) > Integer.parseInt(passengersToAdd.get(j+1).getSeat())) {
                        Passenger temporaryVariable = passengersToAdd.get(j);
                        passengersToAdd.set(j,passengersToAdd.get(j+1));
                        passengersToAdd.set(j+1,temporaryVariable);
                    }
                }
            }
            for (Passenger passenger : passengersToAdd) { //add passengers one by one to queue if it`s not full
                if(!passengerQueue.isFull()) {
                    passengerQueue.add(passenger);
                    addedPassengers.add(passenger);
                }
            }
            for (Passenger passenger : addedPassengers) {
                passengersToAdd.remove(passenger);
            }
            for (int i=0; i <passengersToAdd.size(); i++) {
                waitingRoomList[i] = passengersToAdd.get(i);
            }
            if (passengersToAdd.size() == 0) {
                System.arraycopy(waitingRoomList, addedPassengers.size(), waitingRoomList, 0, waitingRoomList.length - addedPassengers.size());
                for (int i=waitingRoomList.length-addedPassengers.size(); i<waitingRoomList.length; i++) {
                    waitingRoomList[i] = null;
                }
            } else {
                System.arraycopy(waitingRoomList, addedPassengers.size()+passengersToAdd.size(), waitingRoomList, passengersToAdd.size(), waitingRoomList.length - (addedPassengers.size()+passengersToAdd.size()));
                for (int i=waitingRoomList.length-addedPassengers.size(); i<waitingRoomList.length; i++) {
                    waitingRoomList[i] = null;
                }
            }

            System.out.println("Following passengers has been added to the train queue."); //display the details of the passengers who has been added to train queue
            for (Passenger passenger : addedPassengers) {
                System.out.println("        Passenger Name: " + passenger.getName());
                System.out.println("        Passenger NIC Number: " + passenger.getId());
                System.out.println("        Seat Number: " + passenger.getSeat());
                System.out.println();
            }

            GridPane gridlayout1 = new GridPane(); //create GUI to show waiting room
            gridlayout1.setStyle("-fx-background-color:#D9F00E");
            gridlayout1.setPadding(new Insets(20, 20, 40, 20));
            gridlayout1.setVgap(25);
            gridlayout1.setHgap(25);

            int xPosition = 11, yPosition = 6;
            for (int i=0; i<SEATING_CAPACITY; i++) { //create 42 buttons to represent waiting room seats
                Button seatButton = new Button();
                if (waitingRoomList[i] != null) {
                    seatButton.setText("Passenger: " + waitingRoomList[i].getName() + "\n Seat Number: " + waitingRoomList[i].getSeat());
                    seatButton.setStyle("-fx-background-color:blue;-fx-text-fill:white");
                } else {
                    seatButton.setText("Empty");
                    seatButton.setStyle("-fx-background-color:red;-fx-text-fill:white");
                }
                seatButton.setPrefSize(250,180);
                seatButton.setFont(new Font(18));
                gridlayout1.add(seatButton, xPosition, yPosition);
                if ((i+1) % 7 == 0) {    //set positions of the seat buttons
                    xPosition -= 1;
                    yPosition = 6;
                } else {
                    yPosition += 1;
                }
            }

            Label descriptionLabel = new Label("Here`s the current view of the waiting room"); //create labels and other elements
            descriptionLabel.setFont(new Font("Arial Rounded MT Bold", 24));
            Label path = new Label("Out >>>>");
            path.setFont(new Font("Arial Rounded MT Bold", 26));
            Separator separator = new Separator(Orientation.VERTICAL);
            Label trainQueueLabel = new Label("Passenger Queue  >>>>");
            trainQueueLabel.setFont(new Font("Arial Rounded MT Bold", 26));
            Button viewQueue = new Button("Show Passenger Queue");

            gridlayout1.add(descriptionLabel, 8, 1, 10, 1); //add created elements into grid pane
            gridlayout1.add(path, 12, 6, 10, 1);
            gridlayout1.add(separator, 12, 7, 10, 6);
            gridlayout1.add(trainQueueLabel,14 , 9, 15, 1);
            gridlayout1.add(viewQueue,16 , 11, 12, 1);

            viewQueue.setOnAction(event -> {
                Stage currentStage = (Stage) viewQueue.getScene().getWindow();
                currentStage.close(); //close current GUI
            });

            Scene scene1 = new Scene(gridlayout1, 3000, 1000);
            Stage primaryStage1 = new Stage();
            primaryStage1.setTitle("Passenger Boarding Management System - Waiting Room");
            primaryStage1.setScene(scene1);
            primaryStage1.showAndWait();

            GridPane gridlayout2 = new GridPane(); //create GUI to show train queue
            gridlayout2.setStyle("-fx-background-color:#53FADE");
            gridlayout2.setPadding(new Insets(20, 20, 40, 20));
            gridlayout2.setVgap(25);
            gridlayout2.setHgap(25);

            int queueXPosition = 12, queueYPosition = 5;
            for (int i=0; i<passengerQueue.getQueueArray().length; i++) {  //create buttons to represent train queue
                Button seatButton = new Button();
                if (passengerQueue.getQueueArray()[i] != null) {
                    if (addedPassengers.contains(passengerQueue.getQueueArray()[i])) {
                        seatButton.setText(passengerQueue.getQueueArray()[i].getName() + "\n Seat: " + passengerQueue.getQueueArray()[i].getSeat());
                        seatButton.setStyle("-fx-background-color:#72ED1A;-fx-text-fill:white");
                    } else {
                        seatButton.setText(passengerQueue.getQueueArray()[i].getName() + "\n Seat: " + passengerQueue.getQueueArray()[i].getSeat());
                        seatButton.setStyle("-fx-background-color:blue;-fx-text-fill:white");
                    }
                } else {
                    seatButton.setText("Empty");
                    seatButton.setStyle("-fx-background-color:red;-fx-text-fill:white");
                }
                seatButton.setPrefSize(250, 200);
                seatButton.setFont(new Font(18));
                gridlayout2.add(seatButton, queueXPosition, queueYPosition);
                if ( 6<i && i<10 || 15<i && i<19) { //set positions of the train queue buttons
                    queueYPosition += 1;
                } else if (9<i && i<16) {
                    queueXPosition += 1;
                }
                else {
                    queueXPosition -= 1;
                }
            }

            Label queueDescriptionLabel = new Label("Here`s the current view of the passenger queue"); //create labels and other elements
            queueDescriptionLabel.setFont(new Font("Arial Rounded MT Bold", 24));
            Label boardingGateLabel = new Label("Boarding Gate  >>>>");
            boardingGateLabel.setFont(new Font("Arial Rounded MT Bold", 26));
            Rectangle greenRectangle = new Rectangle(70, 50);
            greenRectangle.setStyle("-fx-fill:#72ED1A");
            Label addedPassengersLabel = new Label("Newly Added Passengers");
            addedPassengersLabel.setFont(new Font("Calibri", 20));

            gridlayout2.add(queueDescriptionLabel, 8, 1, 10, 1); //add created elements into grid pane
            gridlayout2.add(boardingGateLabel,14 , 5, 12, 1);
            gridlayout2.add(greenRectangle,14 , 10, 12, 1);
            gridlayout2.add(addedPassengersLabel,17 , 10, 12, 1);

            Scene scene2 = new Scene(gridlayout2, 3000, 1000);
            Stage primaryStage2 = new Stage();
            primaryStage2.setTitle("Passenger Boarding Management System - Passenger Queue");
            primaryStage2.setScene(scene2);
            primaryStage2.showAndWait();

        } else if (checkedInPassengerCount != 0 && boardedPassengerList.size() == checkedInPassengerCount) {
            System.out.println("All the passengers checked in to waiting room have been successfully boarded to the train already.");
        } else if (checkedInPassengerCount != 0 && (passengerQueue.getQueueArrayElementCount() + boardedPassengerList.size()) == checkedInPassengerCount) {
            System.out.println("All the passengers checked in to waiting room have been successfully added to the queue already.");
        } else if (checkedInPassengerCount == 0) {
            System.out.println("None of the passengers has been checked in to waiting room yet.");
        }
    }

    public static void viewTrainQueue(Passenger[] waitingRoomList, PassengerQueue passengerQueue, ArrayList<Passenger> boardedPassengerList) {
        GridPane gridlayout1 = new GridPane();  //create GUI to show train queue
        gridlayout1.setStyle("-fx-background-color:#53FADE");
        gridlayout1.setPadding(new Insets(20, 20, 40, 20));
        gridlayout1.setVgap(25);
        gridlayout1.setHgap(25);

        int queueXPosition = 12, queueYPosition = 5;
        for (int i=0; i<passengerQueue.getQueueArray().length; i++) { //create buttons to represent the train queue
            Button seatButton = new Button();
            if (passengerQueue.getQueueArray()[i] != null) {
                seatButton.setText(passengerQueue.getQueueArray()[i].getName() + "\n Seat: " + passengerQueue.getQueueArray()[i].getSeat());
                seatButton.setStyle("-fx-background-color:blue;-fx-text-fill:white");
            } else {
                seatButton.setText("Empty");
                seatButton.setStyle("-fx-background-color:red;-fx-text-fill:white");
            }
            seatButton.setPrefSize(250, 200);
            seatButton.setFont(new Font(18));
            gridlayout1.add(seatButton, queueXPosition, queueYPosition);
            if ( 6<i && i<10 || 15<i && i<19) { //set positions of the train queue buttons
                queueYPosition += 1;
            } else if (9<i && i<16) {
                queueXPosition += 1;
            }
            else {
                queueXPosition -= 1;
            }
        }

        Label queueDescriptionLabel = new Label("Here`s the current view of the passenger queue");   //create labels and other elements
        queueDescriptionLabel.setFont(new Font("Arial Rounded MT Bold", 24));
        Label boardingGateLabel = new Label("Boarding Gate  >>>>");
        boardingGateLabel.setFont(new Font("Arial Rounded MT Bold", 26));
        Button trainCompartment = new Button("Show Train Compartment");

        gridlayout1.add(queueDescriptionLabel, 8, 1, 10, 1);   //add created elements into grid pane
        gridlayout1.add(boardingGateLabel,14 , 5, 12, 1);
        gridlayout1.add(trainCompartment,14 , 10, 12, 1);

        trainCompartment.setOnAction(event -> {
            Stage currentStage = (Stage) trainCompartment.getScene().getWindow();
            currentStage.close(); //close current GUI
        });

        Scene scene1 = new Scene(gridlayout1, 3000, 1000);
        Stage primaryStage1 = new Stage();
        primaryStage1.setTitle("Passenger Boarding Management System - Passenger Queue");
        primaryStage1.setScene(scene1);
        primaryStage1.showAndWait();

        GridPane gridlayout2 = new GridPane(); //create GUI to view all 42 seats in train compartment
        gridlayout2.setPadding(new Insets(20, 20, 20, 20));
        gridlayout2.setVgap(15);
        gridlayout2.setHgap(15);

        int xPosition = 6, yPosition = 6;
        for (int i = 1; i <= SEATING_CAPACITY; i++) {  //create 42 buttons to represent seats in train compartment
            Button seatButton = new Button();
            if (checkedInPassengerSeats.contains(Integer.toString(i))) {  //check whether passenger is in waiting room
                for (Passenger passenger : waitingRoomList) {
                    if (passenger != null) {
                        if (passenger.getSeat().equals(Integer.toString(i))) {
                            seatButton.setText(passenger.getName() + "\n Seat " + passenger.getSeat());
                            seatButton.setStyle("-fx-background-color:blue;-fx-text-fill:white");
                        }
                    }
                }
                for (Passenger passenger : passengerQueue.getQueueArray()) { //check whether passenger is in train queue
                    if (passenger != null) {
                        if (passenger.getSeat().equals(Integer.toString(i))) {
                            seatButton.setText(passenger.getName() + "\n Seat " + passenger.getSeat());
                            seatButton.setStyle("-fx-background-color:#72ED1A;-fx-text-fill:white");
                        }
                    }
                }
                for (Passenger passenger : boardedPassengerList) { //check whether passenger is in train
                    if (passenger != null) {
                        if (passenger.getSeat().equals(Integer.toString(i))) {
                            seatButton.setText(passenger.getName() + "\n Seat " + passenger.getSeat());
                            seatButton.setStyle("-fx-background-color:#E327D0;-fx-text-fill:white");
                        }
                    }
                }
            } else {
                seatButton.setText("Empty");
                seatButton.setStyle("-fx-background-color:red;-fx-text-fill:white");
            }
            seatButton.setPrefSize(170, 100);
            seatButton.setFont(new Font(18));
            gridlayout2.add(seatButton, xPosition, yPosition);
            if (i % 3 == 0 && i % 6 != 0) { //set positions of seat buttons
                xPosition = 20;
            } else if (i % 6 == 0) {
                xPosition = 6;
                yPosition += 1;
            } else {
                xPosition += 1;
            }
        }

        Label descriptionLabel = new Label("Here`s a view of how passengers are currently situated in the boarding process"); //create labels,buttons and other elements
        descriptionLabel.setFont(new Font("Arial Rounded MT Bold", 24));
        Rectangle blueRectangle = new Rectangle(70, 50);
        blueRectangle.setStyle("-fx-fill:blue");
        Label waitingRoom = new Label("In Waiting Room");
        waitingRoom.setFont(new Font(18));
        Rectangle greenRectangle = new Rectangle(70, 50);
        greenRectangle.setStyle("-fx-fill:#72ED1A");
        Label queue = new Label("In Passenger Queue");
        queue.setFont(new Font(18));
        Rectangle purpleRectangle = new Rectangle(70, 50);
        purpleRectangle.setStyle("-fx-fill:#E327D0");
        Label boarded = new Label("Boarded To Train");
        boarded.setFont(new Font(18));
        Rectangle redRectangle = new Rectangle(70, 50);
        redRectangle.setStyle("-fx-fill:red");
        Label empty = new Label("Haven`t Checked In");
        empty.setFont(new Font(18));

        gridlayout2.add(descriptionLabel, 6, 1, 17, 1); //add created elements into the grid pane
        gridlayout2.add(blueRectangle, 34, 8);
        gridlayout2.add(waitingRoom, 35, 8, 17, 1);
        gridlayout2.add(greenRectangle, 34, 9);
        gridlayout2.add(queue, 35, 9, 17, 1);
        gridlayout2.add(purpleRectangle, 34, 10);
        gridlayout2.add(boarded, 35, 10, 17, 1);
        gridlayout2.add(redRectangle, 34, 11);
        gridlayout2.add(empty, 35, 11, 17, 1);

        Scene scene2 = new Scene(gridlayout2, 3000, 1000);
        Stage primaryStage2 = new Stage();
        primaryStage2.setTitle("Passenger Boarding Management System - Train Compartment");
        primaryStage2.setScene(scene2);
        primaryStage2.showAndWait();
    }

    public static void deletePassenger(PassengerQueue passengerQueue) {
        boolean empty = passengerQueue.isEmpty();  //check whether the queue is empty
        if(!empty) {
            Scanner input = new Scanner(System.in);
            System.out.println("Please enter the seat number of the passenger you want to delete from the train queue: ");
            String seat = input.nextLine();
            while (!checkedInPassengerSeats.contains(seat)) {
                System.out.println("No passenger has been checked into waiting room with this seat number. Please check and re-enter the passenger seat number: ");
                seat = input.nextLine();
            }
            boolean contain = false;
            for (Passenger passenger : passengerQueue.getQueueArray()) {  //check whether the passenger that reserved the provided seat number is in queue
                if(passenger != null) {
                    if (passenger.getSeat().equals(seat)) {
                        contain = true;
                        break;
                    }
                }
            }
            while (!contain) {
                System.out.println("There`s no passenger in train queue with this seat number. Please check and re-enter the passenger seat number: ");
                seat = input.nextLine();
                for (Passenger passenger : passengerQueue.getQueueArray()) {
                    if(passenger != null) {
                        if (passenger.getSeat().equals(seat)) {
                            contain = true;
                            break;
                        }
                    }
                }
            }
            for (Passenger passenger : passengerQueue.getQueueArray()) {
                if (passenger != null) {
                    if (passenger.getSeat().equals(seat)) {
                        Passenger deletePassenger = passengerQueue.delete(passenger);  //delete passenger that booked provided seat number from train queue
                        if(deletePassenger != null) {
                            checkedInPassengerSeats.remove(deletePassenger.getSeat());
                            System.out.println();
                            System.out.println("The following passenger has been deleted from the train queue");  //display the details of the deleted passenger
                            System.out.println("        Passenger Name: " + deletePassenger.getName());
                            System.out.println("        NIC Number: " + deletePassenger.getId());
                            System.out.println("        Seat Number: " + deletePassenger.getSeat());
                        }
                    }
                }
            }
        }
    }

    public static void storeDataIntoFile(Passenger[] waitingRoomList, PassengerQueue passengerQueue, ArrayList<Passenger> boardedPassengerList) {  //store train queue data into database
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("PassengerBoardingManagementSystem");

        MongoCollection<Document> collection1 = mongoDatabase.getCollection("WaitingRoomPassengers");
        BasicDBObject waitingRoomDetails = new BasicDBObject();
        collection1.deleteMany(waitingRoomDetails);
        for (Passenger passenger : waitingRoomList) {  //add passengers in waiting room to a collection in database
            if (passenger != null) {
                String name = passenger.getName();
                String id = passenger.getId();
                String seat = passenger.getSeat();
                Document document = new Document("Passenger_Name", name)
                        .append("Passenger_NIC_Number", id)
                        .append("Seat_Number", seat);
                collection1.insertOne(document);
            }
        }
        MongoCollection<Document> collection2 = mongoDatabase.getCollection("TrainQueuePassengers");
        BasicDBObject trainQueueDetails = new BasicDBObject();
        collection2.deleteMany(trainQueueDetails);
        for (Passenger passenger : passengerQueue.getQueueArray()) { //add passengers in train queue to a collection in database
            if (passenger != null) {
                String name = passenger.getName();
                String id = passenger.getId();
                String seat = passenger.getSeat();
                Document document = new Document("Passenger_Name", name)
                        .append("Passenger_NIC_Number", id)
                        .append("Seat_Number", seat);
                collection2.insertOne(document);
            }
        }
        MongoCollection<Document> collection3 = mongoDatabase.getCollection("BoardedPassengers");
        BasicDBObject trainCompartmentDetails = new BasicDBObject();
        collection3.deleteMany(trainCompartmentDetails);
        for (Passenger passenger : boardedPassengerList) { //add passengers in train compartment to a collection in database
            if (passenger != null) {
                String name = passenger.getName();
                String id = passenger.getId();
                String seat = passenger.getSeat();
                Document document = new Document("Passenger_Name", name)
                        .append("Passenger_NIC_Number", id)
                        .append("Seat_Number", seat);
                collection3.insertOne(document);
            }
        }
        System.out.println("All the details related to the passenger boarding process has been successfully stored into the file.");
    }

    public static void loadDataFromFile(Passenger[] waitingRoomList, PassengerQueue passengerQueue, ArrayList<Passenger> boardedPassengerList) { //load train queue data from database to data structure
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("PassengerBoardingManagementSystem");

        MongoCollection<Document> collection1 = mongoDatabase.getCollection("WaitingRoomPassengers");
        FindIterable<Document> waitingRoomPassengers = collection1.find();
        for (Document details : waitingRoomPassengers) { //get waiting room details from database to data structure
            int count = 0;
            for (Passenger passenger : waitingRoomList) {
                if (passenger != null) {
                    count++;
                }
            }
            waitingRoomList[count] = new Passenger();
            waitingRoomList[count].setName(details.getString("Passenger_Name"));
            waitingRoomList[count].setId(details.getString("Passenger_NIC_Number"));
            waitingRoomList[count].setSeat(details.getString("Seat_Number"));
            checkedInPassengerSeats.add(details.getString("Seat_Number"));
            checkedInPassengerCount++;
        }
        MongoCollection<Document> collection2 = mongoDatabase.getCollection("TrainQueuePassengers");
        FindIterable<Document> trainQueuePassengers = collection2.find();
        for (Document details : trainQueuePassengers) { //get train queue details from database to data structure
            int count = 0;
            for (Passenger passenger : passengerQueue.getQueueArray()) {
                if (passenger != null) {
                    count++;
                }
            }
            Passenger passenger = new Passenger();
            passengerQueue.add(passenger);
            passengerQueue.getQueueArray()[count].setName(details.getString("Passenger_Name"));
            passengerQueue.getQueueArray()[count].setId(details.getString("Passenger_NIC_Number"));
            passengerQueue.getQueueArray()[count].setSeat(details.getString("Seat_Number"));
            checkedInPassengerSeats.add(details.getString("Seat_Number"));
            checkedInPassengerCount++;
        }
        MongoCollection<Document> collection3 = mongoDatabase.getCollection("BoardedPassengers");
        FindIterable<Document> boardedPassengers = collection3.find();
        for (Document details : boardedPassengers) { //get train compartment details from database to data structure
            int count = 0;
            for (Passenger passenger : boardedPassengerList) {
                if (passenger != null) {
                    count++;
                }
            }
            Passenger passenger = new Passenger();
            boardedPassengerList.add(passenger);
            boardedPassengerList.get(count).setName(details.getString("Passenger_Name"));
            boardedPassengerList.get(count).setId(details.getString("Passenger_NIC_Number"));
            boardedPassengerList.get(count).setSeat(details.getString("Seat_Number"));
            checkedInPassengerSeats.add(details.getString("Seat_Number"));
            checkedInPassengerCount++;
        }
        System.out.println("All the details related to the passenger boarding process has been successfully loaded from the file.");
    }

    public static void runSimulationAndProduceReport(PassengerQueue passengerQueue, ArrayList<Passenger> boardedPassengerList) throws IOException {  //board passengers in train queue to train compartment
        ArrayList<Passenger> boardedPassengers = new ArrayList<>();
        ArrayList<Integer> boardedPassengerQueueTimes = new ArrayList<>();
        int totalDelay = 0;
        boolean empty = passengerQueue.isEmpty();  //check whether the train queue is empty

        if (!empty) {
            passengerQueue.setMaxLength(passengerQueue.getQueueArrayElementCount());
            for (Passenger passenger : passengerQueue.getQueueArray()) {
                if (passenger != null) {
                    Random randomNumber = new Random();
                    int delayNumber1 = randomNumber.nextInt(6) + 1;
                    int delayNumber2 = randomNumber.nextInt(6) + 1;
                    int delayNumber3 = randomNumber.nextInt(6) + 1;
                    int processingDelay = delayNumber1 + delayNumber2 + delayNumber3;  //create processing delay for every passenger in train queue
                    totalDelay += processingDelay;

                    new java.util.Timer().schedule (  //remove first passenger of the train queue after a given time and board passenger to train
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    for (Passenger queuePassenger : passengerQueue.getQueueArray()) {
                                        if (queuePassenger != null) {
                                            queuePassenger.setSecondsInQueue(processingDelay);
                                        }
                                    }
                                    if (passengerQueue.getQueueArray()[0] != null) {
                                        Passenger boardedPassenger = passengerQueue.remove();
                                        if (boardedPassenger != null) {
                                            boardedPassengerList.add(boardedPassenger);
                                            boardedPassengers.add(boardedPassenger);
                                            boardedPassengerQueueTimes.add(boardedPassenger.getSecondsInQueue());
                                            System.out.println();
                                            System.out.println("The following passenger has been boarded to the train");  //display details of the passenger boarded to train
                                            System.out.println("        Passenger Name: " + boardedPassenger.getName());
                                            System.out.println("        NIC Number: " + boardedPassenger.getId());
                                            System.out.println("        Seat Number: " + boardedPassenger.getSeat());
                                        }
                                    }
                                }
                            }, totalDelay * 1000
                    );
                }
            }

            new java.util.Timer().schedule (  //display message after a given time
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            System.out.println();
                            System.out.println("Passenger boarding process is now complete. Please close the currently opened window to see the generated report.");
                        }
                    }, (totalDelay+1) * 1000
            );

            GridPane gridlayout1 = new GridPane(); //create GUI to show train queue
            gridlayout1.setStyle("-fx-background-color:#22F2E4");
            gridlayout1.setPadding(new Insets(20, 20, 40, 20));
            gridlayout1.setVgap(25);
            gridlayout1.setHgap(25);

            int queueXPosition = 12, queueYPosition = 6;
            for (int i=0; i<passengerQueue.getQueueArray().length; i++) {  //create buttons to represent train queue
                Button seatButton = new Button();
                if (passengerQueue.getQueueArray()[i] != null) {
                    seatButton.setText(passengerQueue.getQueueArray()[i].getName() + "\n Seat: " + passengerQueue.getQueueArray()[i].getSeat());
                    seatButton.setStyle("-fx-background-color:blue;-fx-text-fill:white");
                } else {
                    seatButton.setText("Empty");
                    seatButton.setStyle("-fx-background-color:red;-fx-text-fill:white");
                }
                seatButton.setPrefSize(250, 200);
                seatButton.setFont(new Font(18));
                gridlayout1.add(seatButton, queueXPosition, queueYPosition);
                if ( 6<i && i<10 || 15<i && i<19) { //set positions of the train queue buttons
                    queueYPosition += 1;
                } else if (9<i && i<16) {
                    queueXPosition += 1;
                }
                else {
                    queueXPosition -= 1;
                }
            }

            Label queueDescriptionLabel = new Label("Here`s the current view of the passenger queue");  //create labels and other elements
            queueDescriptionLabel.setFont(new Font("Arial Rounded MT Bold", 24));
            Label boardingMessage = new Label("Passenger boarding process is currently in progress......");
            boardingMessage.setFont(new Font("Calibri", 24));
            Label warning = new Label("Please don`t close the window until you got a message asking to close it in the console!");
            warning.setFont(new Font("Calibri", 24));
            Label boardingGateLabel = new Label("Boarding Gate  >>>>");
            boardingGateLabel.setFont(new Font("Arial Rounded MT Bold", 26));

            gridlayout1.add(queueDescriptionLabel, 8, 1, 10, 1);  //add created elements into grid pane
            gridlayout1.add(boardingMessage, 5, 3, 10, 1);
            gridlayout1.add(warning, 5, 4, 10, 1);
            gridlayout1.add(boardingGateLabel,14 , 6, 12, 1);

            Scene scene1 = new Scene(gridlayout1, 3000, 1000);
            Stage primaryStage1 = new Stage();
            primaryStage1.setTitle("Passenger Boarding Management System - Passenger Queue");
            primaryStage1.setScene(scene1);
            primaryStage1.showAndWait();

            GridPane gridlayout2 = new GridPane(); //create GUI to view the report
            gridlayout2.setPrefWidth(3000);
            gridlayout2.setStyle("-fx-background-color:#D9F00E");
            gridlayout2.setPadding(new Insets(20, 20, 20, 20));
            gridlayout2.setVgap(15);
            gridlayout2.setHgap(15);

            for (int i=0; i<boardedPassengerQueueTimes.size(); i++) { //sort passenger waiting times in queue using bubble sorting algorithm
                for (int j=0; j<boardedPassengerQueueTimes.size()-i-1;j++ ) {
                    if (boardedPassengerQueueTimes.get(j) > boardedPassengerQueueTimes.get(j+1)) {
                        int temporaryVariable = boardedPassengerQueueTimes.get(j);
                        boardedPassengerQueueTimes.set(j,boardedPassengerQueueTimes.get(j+1));
                        boardedPassengerQueueTimes.set(j+1,temporaryVariable);
                    }
                }
            }

            double averageWaiting = 0.0;
            if (boardedPassengers.size() != 0 && boardedPassengerQueueTimes.size() != 0) {
                double totalWaiting = 0.0;
                passengerQueue.setMaxStayInQueue(boardedPassengerQueueTimes.get(boardedPassengerQueueTimes.size() - 1));
                for (Integer time : boardedPassengerQueueTimes) {
                    totalWaiting += time;
                }
                averageWaiting = (double) Math.round(totalWaiting/boardedPassengerQueueTimes.size() * 100.0) / 100.0;  //get average waiting time for passengers in queue

                Label message = new Label("This is the report that produced based on the passenger boarding process"); //create labels,buttons and other elements
                message.setFont(new Font("Arial Rounded MT Bold", 24));
                Label maxLength = new Label("Maximum length of the passenger queue: " + passengerQueue.getMaxLength() + " Passengers");
                maxLength.setFont(new Font("Calibri", 22));
                Label maxWaitingTime = new Label("Maximum waiting time in the passenger queue: " +  passengerQueue.getMaxStayInQueue() + " Seconds");
                maxWaitingTime.setFont(new Font("Calibri", 22));
                Label minWaitingTime = new Label("Minimum waiting time in the passenger queue: " + boardedPassengerQueueTimes.get(0) + " Seconds");
                minWaitingTime.setFont(new Font("Calibri", 22));
                Label averageWaitingTime = new Label("Average waiting time for passengers in passenger queue: " + averageWaiting + " Seconds");
                averageWaitingTime.setFont(new Font("Calibri", 22));
                Label passengerDetails = new Label("This is a list of passengers who has been boarded to the train compartment through this boarding process");
                passengerDetails.setFont(new Font("Calibri", 22));

                int xPosition = 18, yPosition = 15;
                for (Passenger passenger : boardedPassengers) {  //create labels to view report details
                    if (passenger != null) {
                        Label name = new Label("Passenger Name : " + passenger.getName());
                        name.setFont(new Font("Arial Rounded MT Bold", 20));
                        gridlayout2.add(name, xPosition, yPosition, 8, 1);
                        Label id = new Label("Passenger NIC Number : " + passenger.getId());
                        id.setFont(new Font("Arial Rounded MT Bold", 20));
                        gridlayout2.add(id, xPosition, yPosition + 1, 8, 1);
                        Label seatNumber = new Label("Seat Number : " + passenger.getSeat());
                        seatNumber.setFont(new Font("Arial Rounded MT Bold", 20));
                        gridlayout2.add(seatNumber, xPosition, yPosition + 2, 8, 1);
                        Label waitingTime = new Label("Waiting Time In Queue : " + passenger.getSecondsInQueue() + " Seconds");
                        waitingTime.setFont(new Font("Arial Rounded MT Bold", 20));
                        gridlayout2.add(waitingTime, xPosition, yPosition + 3, 8, 1);
                        yPosition += 6;
                    }
                }

                gridlayout2.add(message, 22, 2, 40, 1); //add created elements into the grid pane
                gridlayout2.add(maxLength, 15, 6, 8, 1);
                gridlayout2.add(maxWaitingTime, 15, 7, 8, 1);
                gridlayout2.add(minWaitingTime, 15, 8, 8, 1);
                gridlayout2.add(averageWaitingTime, 15, 9, 8, 1);
                gridlayout2.add(passengerDetails, 10, 12, 40, 1);
            }

            ScrollPane scrollPane = new ScrollPane(gridlayout2);
            scrollPane.setFitToHeight(true);

            Scene scene2 = new Scene(scrollPane, 3000, 1000);
            Stage primaryStage2 = new Stage();
            primaryStage2.setTitle("Passenger Boarding Management System - Boarding Process Report");
            primaryStage2.setScene(scene2);
            primaryStage2.showAndWait();

            try {
                FileWriter fileWriter = new FileWriter("Boarding Process Report.txt",true);  //add report data into a text file
                fileWriter.write("\n \n This is the report that produced based on the passenger boarding process");
                fileWriter.write("\n \n Maximum length of the passenger queue: " + passengerQueue.getMaxLength() + " Passengers");
                fileWriter.write("\n Maximum waiting time in the passenger queue: " + passengerQueue.getMaxStayInQueue() + " Seconds");
                fileWriter.write("\n Minimum waiting time in the passenger queue: " + boardedPassengerQueueTimes.get(0) + " Seconds");
                fileWriter.write("\n Average waiting time for passengers in passenger queue: " + averageWaiting + " Seconds");
                fileWriter.write("\n \n This is a list of passengers who has been boarded to the train compartment through this boarding process");
                for (Passenger passenger : boardedPassengers) {
                    if (passenger != null) {
                        fileWriter.write("\n \n Passenger Name : " + passenger.getName());
                        fileWriter.write("\n Passenger NIC Number : " + passenger.getId());
                        fileWriter.write("\n Seat Number : " + passenger.getSeat());
                        fileWriter.write("\n Waiting Time In Queue : " + passenger.getSecondsInQueue() + " Seconds");
                    }
                }
                fileWriter.close();
            } catch (FileNotFoundException e) {
                System.out.println("The file you`re trying to modify cannot be found.");
            }
        }
    }

}
