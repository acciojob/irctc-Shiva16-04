package com.driver.Transformers;

import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;

public class TicketTransformer {
    public static Ticket bookTicketEntryDTOToTicket(BookTicketEntryDto bookTicketEntryDto){
        Ticket ticket=new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        return ticket;
    }
}
