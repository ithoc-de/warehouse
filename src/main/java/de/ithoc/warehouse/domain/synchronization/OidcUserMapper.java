package de.ithoc.warehouse.domain.synchronization;

import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.persistence.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OidcUserMapper {

    UserInput toUserInput(User user);
    User toUser(UserInput userInput);

}
