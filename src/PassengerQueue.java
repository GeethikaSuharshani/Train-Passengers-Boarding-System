public class PassengerQueue {
    private Passenger[] queueArray;
    private int first;
    private int last;
    private int maxStayInQueue;
    private int maxLength;

    public PassengerQueue(int arraySize) { //constructor for passenger queue objects
        super();
        this.queueArray = new Passenger[arraySize]; //initialize an array of passenger objects with the size of passed integer value to parameter "arraySize"
        this.first = -1;
        this.last = -1;
        this.maxStayInQueue = 0;
        this.maxLength = 0;
    }

    public Passenger[] getQueueArray() { //return train queue
        return queueArray;
    }

    public int getQueueArrayElementCount() { //return number of passengers currently in the train queue
        int count = 0;
        for (Passenger passenger : this.queueArray) {
            if (passenger != null) {
                count++;
            }
        }
        return count;
    }

    public void add(Passenger next) { //add a passenger object to the train queue
        boolean full = this.isFull();
        if (!full) { //add passenger object at the end of the train queue and set first and last values of array
            if (this.first == -1) {
                this.first = this.last = 0;
                this.queueArray[this.last] = next;
            } else if (this.last == this.queueArray.length - 1 && this.first != 0) {
                this.last = 0;
                this.queueArray[this.last] = next;
            } else {
                this.last++;
                this.queueArray[this.last] = next;
            }
        }
    }

    public Passenger remove() { //remove the first element in train queue
        boolean empty = this.isEmpty();
        if (!empty) {
            Passenger passenger = this.queueArray[this.first];
            if(passenger != null) {
                this.queueArray[this.first] = null;  //make the first element in the train queue array null
                if (this.first < this.getQueueArray().length - 1) {
                    System.arraycopy(this.queueArray, this.first + 1, this.queueArray, 0, this.getQueueArray().length - 1);
                    this.getQueueArray()[this.last] = null;
                }
                if (this.first == this.last) {  //set first and last values of the array after removing the first element
                    this.first = -1;
                    this.last = -1;
                } else if (this.first == 0 && this.last <= this.getQueueArray().length - 1) {
                    this.last = this.last - 1;
                } else if (this.first != 0 && this.last <= this.getQueueArray().length - 1) {
                    this.last = this.last - (this.first+1);
                    this.first = 0;
                } else if (this.last - this.first < 0) {
                    this.last = this.last + (this.getQueueArray().length - (this.first+1));
                    this.first = 0;
                }
                return passenger;
            }
        }
        return null;
    }

    public Passenger delete(Passenger deletePassenger) {  //delete a given element in train queue array and reorder array
        boolean empty = this.isEmpty();
        if (empty) {
            System.out.println("There`s no passengers to delete from the queue.");
            return null;
        }
        for (int i = 0; i < this.getQueueArray().length; i++) {
            if(this.getQueueArray()[i] != null) {
                if (this.getQueueArray()[i].equals(deletePassenger)) {
                    this.getQueueArray()[i] = null;  //make the given element at train queue array null
                    if (i<this.getQueueArray().length - 1) {
                        System.arraycopy(this.queueArray, i + 1, this.queueArray, i, this.getQueueArray().length - (i + 1));
                        this.getQueueArray()[this.last] = null;
                    }
                }
            }
        }
        if (this.first == this.last) { //set first and last values of the array after deleting the given element
            this.first = -1;
            this.last = -1;
        } else if (this.first == 0 && this.last <= this.getQueueArray().length - 1) {
            this.last = this.last - 1;
        } else if (this.first != 0 && this.last <= this.getQueueArray().length - 1) {
            System.arraycopy(this.queueArray, this.first, this.queueArray, 0, this.last - this.first);
            this.last = this.last - (this.first+1);
            this.first = 0;
        } else if (this.last - this.first < 0) {
            System.arraycopy(this.queueArray, this.first, this.queueArray, 0, this.last + (this.getQueueArray().length - this.first));
            this.last = this.last + (this.getQueueArray().length - (this.first+1));
            this.first = 0;
        }
        return deletePassenger;
    }

    public boolean isEmpty() { //check whether train queue is empty
        if (this.first == -1) {
            System.out.println("Passenger queue is empty at this moment.");
            return true;
        } else {
            return false;
        }
    }

    public boolean isFull() { //check whether train queue is full
        if ((this.last == this.queueArray.length-1 && this.first == 0) || this.last == (this.first-1)%(this.queueArray.length-1)) {
            System.out.println("Sorry, the queue is full. Maximum limit of passengers has been added to the queue already.");
            return true;
        } else {
            return false;
        }
    }

    public int getMaxStayInQueue() { //return maximum waiting time of passenger in queue
        return maxStayInQueue;
    }

    public void setMaxStayInQueue(int maxStayInQueue) { //set a value for maximum waiting time of passenger in queue
        this.maxStayInQueue = maxStayInQueue;
    }

    public int getMaxLength() { //return the maximum length of the train queue
        return maxLength;
    }

    public void setMaxLength(int maxLength) { //set a value for maximum length of the train queue
        this.maxLength = maxLength;
    }
}
