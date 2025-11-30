package com.flipfoundry.tutorial.api.application.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * <p>The Greeting DTO containing the following:</p>
 * <ul>
 *  <li>An Id
 *  <li>A content field
 * </ul>
 *
 * @deprecated As of release 1.3, moved to {@link GreetingDTOV2}
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated(since = "1.3", forRemoval = false)
public class GreetingDTO {

    /**
     * @since 1.0
     */
    private long id;
    /**
     * @since 1.0
     */
    private String content;

}
