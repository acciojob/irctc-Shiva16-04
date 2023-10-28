package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.Transformers.TrainTransformer;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

import static javax.management.Query.plus;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;


    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train=trainRepository.save(TrainTransformer.addTrainEntryDTOToTrain(trainEntryDto));
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        int occupiedSeats=0;
        Optional<Train>trainOptional=trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if(trainOptional.isPresent()){
            Train train=trainOptional.get();
            List<String>trainList= Arrays.asList(train.getRoute().split(","));
            int []seatsAvailability=new int[trainList.size()];
            HashMap<String, Integer>index=new HashMap<>();
            for(int i=0; i<trainList.size(); i++){
                index.put(trainList.get(i),i);
            }
            int entryIndex=index.get(seatAvailabilityEntryDto.getFromStation().toString());
            int exitIndex=index.get(seatAvailabilityEntryDto.getToStation().toString());
            List<Ticket>bookedTickets=trainOptional.get().getBookedTickets();
            for(Ticket ticket:bookedTickets){
                String fromStation=ticket.getFromStation().toString();
                String toStation=ticket.getToStation().toString();
                int cnt=ticket.getPassengersList().size();
                seatsAvailability[index.get(fromStation)]+=cnt;
                seatsAvailability[index.get(toStation)]-=cnt;
            }
            for(int i=1; i<seatsAvailability.length; i++){
                seatsAvailability[i]=seatsAvailability[i]+seatsAvailability[i-1];
            }
            for(int i=entryIndex; i<=exitIndex; i++){
                occupiedSeats=Math.max(occupiedSeats, seatsAvailability[i]);
            }
        }

       return trainOptional.get().getNoOfSeats()-occupiedSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{
        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        String entryStation=station.toString();
        Optional<Train>trainOptional=trainRepository.findById(trainId);
        int cnt=0;
        if(trainOptional.isPresent()) {
            Train train = trainOptional.get();
            List<String> trainList = Arrays.asList(train.getRoute().split(","));
            if(trainList.contains(entryStation)==false){
                throw new Exception("Train is not passing from this station");
            }
            List<Ticket>bookedTickets=trainOptional.get().getBookedTickets();
            for(Ticket ticket:bookedTickets){
                String fromStation=ticket.getFromStation().toString();
                if(fromStation.equals(entryStation))cnt+=ticket.getPassengersList().size();
            }
        }
        return cnt;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train>trainOptional=trainRepository.findById(trainId);
        int oldestAge=0;
        if(trainOptional.isPresent()) {
            Train train = trainOptional.get();
            List<Ticket>bookedTickets=trainOptional.get().getBookedTickets();
            for(Ticket ticket:bookedTickets){
                for(Passenger passenger: ticket.getPassengersList()){
                    oldestAge=Math.max(oldestAge, passenger.getAge());
                }
            }
        }
        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train>trainList=trainRepository.findAll();
        List<Integer>ansList=new ArrayList<>();
        for(Train train : trainList){
            List<String>stationList=Arrays.asList(train.getRoute().split(","));
            for(int i=0; i<stationList.size(); i++) {
                if (stationList.get(i).equals(station.toString()) == true) {
                    int hr=startTime.getHour()+i;
                    int min=startTime.getMinute();
                    LocalTime time = LocalTime.of(hr,min);
                    if((time.isAfter(startTime)||time.equals(startTime)) && (time.isBefore(endTime) || time.equals(endTime))){
                        ansList.add(train.getTrainId());
                    }
                }
            }
        }
        return ansList;
    }

}
