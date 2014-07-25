package org.mifosplatform.workflow.eventaction.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.mifosplatform.crm.ticketmaster.data.TicketMasterData;
import org.mifosplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.mifosplatform.finance.billingorder.api.BillingOrderApiResourse;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.domain.MessageDataRepository;
import org.mifosplatform.organisation.message.exception.EmailNotFoundException;
import org.mifosplatform.portfolio.association.data.AssociationData;
import org.mifosplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.data.EventActionProcedureData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EventActionWritePlatformServiceImpl implements ActiondetailsWritePlatformService{
	
	
	
	private final EventActionRepository eventActionRepository;
    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;	
    private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
    private final OrderRepository orderRepository;
    private final BillingOrderApiResourse billingOrderApiResourse;
    private static MessageDataRepository messageDataRepository;
    private static BillingMessageTemplateRepository messageTemplateRepository;
    private TicketMasterReadPlatformService ticketMasterReadPlatformService ;
    private final AppUserReadPlatformService readPlatformService;

	@Autowired
	public EventActionWritePlatformServiceImpl(final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final EventActionRepository eventActionRepository,
			final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,final OrderRepository orderRepository,final BillingOrderApiResourse billingOrderApiResourse,
			final MessageDataRepository messageDataRepository,final BillingMessageTemplateRepository messageTemplateRepository,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,
			final AppUserReadPlatformService readPlatformService)
	{
		this.eventActionRepository=eventActionRepository;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
        this.contractPeriodReadPlatformService=contractPeriodReadPlatformService;
        this.orderRepository=orderRepository;
        this.billingOrderApiResourse=billingOrderApiResourse;
        this.messageDataRepository=messageDataRepository;
        this.messageTemplateRepository=messageTemplateRepository;
        this.ticketMasterReadPlatformService=ticketMasterReadPlatformService;
        this.readPlatformService=readPlatformService;
	}
	
	
	@Override
	public void AddNewActions(List<ActionDetaislData> actionDetaislDatas,final Long clientId,final String resourceId) {
    
		try{
    	
		if(actionDetaislDatas!=null){
			
			 EventAction eventAction=null;
			
			for(ActionDetaislData detailsData:actionDetaislDatas){
				
				//if(EventActionConstants.EVENT_CREATE_PAYMENT.equalsIgnoreCase(detailsData.getaActionName())){}
				
				     EventActionProcedureData actionProcedureData=this.actionDetailsReadPlatformService.checkCustomeValidationForEvents(clientId, detailsData.getEventName(),detailsData.getaActionName(),resourceId);
				     JSONObject jsonObject=new JSONObject();
				     if(actionProcedureData.isCheck()){
				    	 
				    	  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
				    	// JSONObject jsonObject=new JSONObject();
				    	
				    	 List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyContractType("Month(s)",1);
				    	 
				    	 if(detailsData.getaActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_EMAIL)){
				        	  	TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				        	  	AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				        	  	
				        	  	BillingMessageTemplate billingMessageTemplate = messageTemplateRepository.findOne((long) 11);
				        	  	if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"\n"+data.getStatusDescription(), "", user.getEmail(), user.getEmail(),
											"Ticket:"+resourceId, "N", billingMessageTemplate,'E');
									messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  		
				        	  			throw new EmailNotFoundException(new Long(data.getUserId()));
					        	  		
					        	  	}else{
				        	  		BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"\n"+data.getStatusDescription(), "", actionProcedureData.getEmailId(), actionProcedureData.getEmailId(),
											"Ticket:"+resourceId, "N", billingMessageTemplate,'E');
									messageDataRepository.save(billingMessage);
					        	  	}
				        	  	}
								
				          }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_ACTIVE)){
					  
					          AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
 					         
					          if(associationData ==null){
					        	  
						           throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
					            }
					          
   					    	 jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
				        	 jsonObject.put("description","Order Renewal By Scheduler");
				        	  eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
				        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				        	  this.eventActionRepository.save(eventAction);
				         
				           }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_NEW)){
				        	  
				        	   jsonObject.put("billAlign","false");
				        	   jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				        	   jsonObject.put("dateFormat","dd MMMM yyyy");
                               jsonObject.put("locale","en");
				        	   jsonObject.put("paytermCode","Monthly");
				        	   jsonObject.put("planCode",actionProcedureData.getPlanId());
				        	   jsonObject.put("isNewplan","true");
				        	   jsonObject.put("start_date",dateFormat.format(new Date()));
				        	   
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
					        			 Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
				        	   
				        	   this.eventActionRepository.save(eventAction);
				        	   
				           }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_DISCONNECT)){
				        	   
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
				        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				        	   this.eventActionRepository.save(eventAction);
				        	   	
				          }else if(detailsData.getaActionName().equalsIgnoreCase(EventActionConstants.ACTION_INVOICE)){
				        	  	
				        	  	
				        	  jsonObject.put("dateFormat","dd MMMM yyyy");
                              jsonObject.put("locale","en");
				        	  jsonObject.put("systemDate",dateFormat.format(new Date()));
				        	  		
				        	  if(detailsData.IsSynchronous().equalsIgnoreCase("N")){
				        		  
				        	  eventAction=new EventAction(new Date(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString() ,EventActionConstants.ACTION_INVOICE.toString(),"/billingorder/"+clientId, 
						        	   Long.parseLong(resourceId), jsonObject.toString(),Long.parseLong(resourceId),clientId);
						        	   this.eventActionRepository.save(eventAction);
				              }else{
				            	  
				            	  Order order=this.orderRepository.findOne(new Long(resourceId));
				            	  
				            	  
				  			//JsonObject jsonObject3=new JsonObject();
				            	  jsonObject.put("dateFormat","dd MMMM yyyy");
	                              jsonObject.put("locale","en");
					        	  jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
					        	  
				  			this.billingOrderApiResourse.retrieveBillingProducts(order.getClientId(),jsonObject.toString());
				  			
				          }
				        }
				           
				  } if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_PROVISION)){
		        	   
		        	   eventAction=new EventAction(new Date(), "CREATE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(),"/processrequest/"+clientId, 
		        	   Long.parseLong(resourceId),jsonObject.toString(),clientId,clientId);
		        	   this.eventActionRepository.save(eventAction);
		        	   
		          }
			}
		}
		
    }catch(Exception exception){
    	exception.printStackTrace();
    }
    
   
	}

	
}
