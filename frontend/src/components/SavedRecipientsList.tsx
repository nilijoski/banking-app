import type {SavedRecipient} from '../types';
import { formatIban } from '../utils/formatters';

interface SavedRecipientsListProps {
    recipients: SavedRecipient[];
    onDelete: (iban: string) => void;
}

function SavedRecipientsList({ recipients, onDelete }: Readonly<SavedRecipientsListProps>) {
    if (recipients.length === 0) {
        return <p style={{ textAlign: 'center', color: '#999', padding: '40px' }}>No saved recipients yet</p>;
    }

    return (
        <div className="contacts-section">
            <div className="transactions-list">
                {recipients.map((r) => (
                    <div key={r.id} className="transaction-item" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
                        <div className="transaction-details">
                            <div className="transaction-recipient">{r.firstName} {r.lastName}</div>
                            <div className="transaction-iban">{formatIban(r.iban)}</div>
                        </div>
                        <button
                            onClick={() => onDelete(r.iban)}
                            style={{ background: '#dc3545', padding: '8px 16px', marginTop: '10px' }}
                        >
                            Delete
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default SavedRecipientsList;