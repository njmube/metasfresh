package de.metas.document.archive.model.validator;

import java.util.Properties;

import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.ad.modelvalidator.annotations.Validator;
import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.ModelValidator;

import de.metas.document.archive.api.IBPartnerBL;
import de.metas.document.archive.api.IDocOutboundDAO;
import de.metas.document.archive.model.I_AD_User;
import de.metas.document.archive.model.I_C_BPartner;
import de.metas.document.archive.model.I_C_Doc_Outbound_Log;

/**
 * sync flags
 * 
 * @author cg
 * 
 */
@Validator(I_AD_User.class)
class AD_User
{

	@ModelChange(timings = { ModelValidator.TYPE_AFTER_NEW, ModelValidator.TYPE_AFTER_CHANGE }, ifColumnsChanged = I_C_Doc_Outbound_Log.COLUMNNAME_IsInvoiceEmailEnabled)
	public void updateFlag(final I_AD_User user)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(user);

		final I_C_BPartner bpartner = InterfaceWrapperHelper.create(user.getC_BPartner(), I_C_BPartner.class);
		final boolean isInvoiceEmailEnabled  = Services.get(IBPartnerBL.class).isInvoiceEmailEnabled(bpartner, user);
		
		//
		//retrieve latest log
		final I_C_Doc_Outbound_Log docExchange = Services.get(IDocOutboundDAO.class).retrieveLog(ctx, user.getC_BPartner_ID(), Services.get(IADTableDAO.class).retrieveTableId(I_C_Invoice.Table_Name));
		//
		// update outbound log accordingly which will trigger a validator <code>C_Doc_Outbound_Log</code> which will create the notification
		docExchange.setIsInvoiceEmailEnabled(isInvoiceEmailEnabled);
		InterfaceWrapperHelper.save(docExchange);
	}

}
