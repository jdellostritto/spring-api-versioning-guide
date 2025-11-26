package com.flipfoundry.tutorial.api.application.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipfoundry.tutorial.api.application.web.dto.DepartDTO;

import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>The Depart controller implements an endpoint that returns a goodbye message
 * to the caller. Returns departure information with timestamp.</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.1
 */
@RestController
@RequestMapping(value = "/flip/departing/")
public class DepartController {

    /**
     * <p>Depart endpoint.</p>
     *
     *
     * @return mono         The Depart DTO.
     * @see DepartDTO
     * @since 1.0
     *
     */

    @GetMapping(value = "/depart", produces="application/vnd.flipfoundry.departing.v1+json")
    public Mono<DepartDTO> depart() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(timestamp.getTime());
        // S is the millisecond
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");
        //Update for 1.1 using the extended DepartDTO with added date.
        return Mono.just( new DepartDTO("Goodbye",simpleDateFormat.format(date)));
    }

}
