/**
 * Copyright 2016 SmartBear Software
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.interiot.intermw.api.rest.resource;

import eu.interiot.intermw.api.exception.BadRequestException;
import eu.interiot.intermw.api.exception.ConflictException;
import eu.interiot.intermw.api.exception.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InterMwExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LogManager.getLogger(InterMwExceptionMapper.class);

    public Response toResponse(Exception ex) {
        if (ex instanceof ConflictException) {
            return Response
                    .status(Status.CONFLICT)
                    .entity(ex.getMessage()).build();

        } else if (ex instanceof NotFoundException) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ex.getMessage()).build();

        } else if (ex instanceof BadRequestException) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .entity(ex.getMessage()).build();

        } else {
            logger.error("An unexpected error occurred: " + ex.getMessage(), ex);

            return Response.serverError()
                    .entity("An unexpected error occurred. Please see server log for details.")
                    .build();
        }
    }
}