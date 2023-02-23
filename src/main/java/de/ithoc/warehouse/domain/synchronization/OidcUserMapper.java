package de.ithoc.warehouse.domain.synchronization;

import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OidcUserMapper {

    UserInput toUserInput(User user);
    User toUser(UserInput userInput);

}
