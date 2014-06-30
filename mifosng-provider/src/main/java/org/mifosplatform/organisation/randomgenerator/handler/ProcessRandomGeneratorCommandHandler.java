package org.mifosplatform.organisation.randomgenerator.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.randomgenerator.service.RandomGeneratorWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessRandomGeneratorCommandHandler implements NewCommandSourceHandler {

	private final RandomGeneratorWritePlatformService writePlatformService;

    @Autowired
    public ProcessRandomGeneratorCommandHandler(final RandomGeneratorWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
    	
    	return this.writePlatformService.GenerateVoucherPinKeys(command.entityId());
		
	}
    
}
