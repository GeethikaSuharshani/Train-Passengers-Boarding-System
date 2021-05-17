public class Passenger {
    private String name;
    private String id;
    private String seat;
    private int secondsInQueue;

    public String getName() { //return passenger name
        return name;
    }

    public void setName(String name) { //set a value for passenger name
        this.name = name;
    }

    public String getId() { //return passenger NIC number
        return id;
    }

    public void setId(String id) { //set a value for passenger NIC number
        this.id = id;
    }

    public String getSeat() { //return passenger seat number
        return seat;
    }

    public void setSeat(String seat) { //set a value for passenger seat number
        this.seat = seat;
    }

    public int getSecondsInQueue() { //return waiting time of the passenger in train queue
        return secondsInQueue;
    }

    public void setSecondsInQueue(int delayTime) { //set a value for waiting time of the passenger in train queue
        this.secondsInQueue = this.secondsInQueue + delayTime;
    }
}
