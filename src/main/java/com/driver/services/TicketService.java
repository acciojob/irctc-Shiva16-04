package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.Transformers.TicketTransformer;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        String fromStation=bookTicketEntryDto.getFromStation().toString();
        String toStation=bookTicketEntryDto.getToStation().toString();
        int i=-1;
        int j=-1;
        int cnt=0;

        //Handling 1st edge case
        Optional<Train> trainOptional=trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(trainOptional.isPresent()){
            Train train =trainOptional.get();
            List<String>stationList= Arrays.asList(train.getRoute().split(","));

            for(int k=0; k<stationList.size(); k++){
                if(stationList.get(k).equals(fromStation))i=k;
                if(stationList.get(k).equals(toStation))j=k;
            }
            if(i==-1||j==-1){
                throw new Exception("Invalid stations");
            }
        }

        //handling 2nd edge case
        if(trainOptional.isPresent()){
            List<Ticket>bookedTickets=trainOptional.get().getBookedTickets();
            for(Ticket ticket:bookedTickets){
                cnt+=ticket.getPassengersList().size();
            }
            if(cnt+bookTicketEntryDto.getNoOfSeats()>trainOptional.get().getNoOfSeats()){
                throw new Exception("Less tickets are available");
            }
        }

        //adding  passengers to the tickets
        Ticket ticket= TicketTransformer.bookTicketEntryDTOToTicket(bookTicketEntryDto);
        for(int Id:bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> passengerOptional=passengerRepository.findById(Id);
            if(passengerOptional.isPresent())
            ticket.getPassengersList().add(passengerOptional.get());
        }


        //setting train
        if(trainOptional.isPresent())ticket.setTrain(trainOptional.get());


        //calculating fare
        int fare=(j-i)*300*cnt;
        ticket.setTotalFare(fare);


        //bidirectional mapping of ticket with the person who booked ticket
        Optional<Passenger> passengerBookedOptional=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if(passengerBookedOptional.isPresent())passengerBookedOptional.get().getBookedTickets().add(ticket);

        //bidirectional mapping of ticket with train
        if(trainOptional.isPresent())trainOptional.get().getBookedTickets().add(ticket);

        Ticket savedTicket=ticketRepository.save(ticket);

        return savedTicket.getTicketId();

    }
}
