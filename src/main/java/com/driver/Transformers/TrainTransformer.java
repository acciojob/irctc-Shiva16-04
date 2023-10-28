package com.driver.Transformers;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.model.Station;
import com.driver.model.Train;

import java.util.ArrayList;
import java.util.List;

public class TrainTransformer {
    public static Train addTrainEntryDTOToTrain(AddTrainEntryDto addTrainEntryDto){
        Train train=new Train();
        train.setDepartureTime(addTrainEntryDto.getDepartureTime());
        train.setNoOfSeats(addTrainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());
        List<Station> stationList=addTrainEntryDto.getStationRoute();
        String route="";
        for(int i=0; i< stationList.size();i++){
            route+=stationList.get(i);
            if(i!= stationList.size()-1)route+=",";
        }
        train.setRoute(route);
        return train;
    }
}
