import type {Transaction} from '../types/types.ts';
import { formatIban, formatDate } from '../utils/formatters';

interface TransactionsListProps {
    transactions: Transaction[];
    userIban: string;
}

function TransactionsList({ transactions, userIban }: Readonly<TransactionsListProps>) {
    if (transactions.length === 0) {
        return <p style={{ textAlign: 'center', color: '#999', padding: '40px' }}>No transactions yet</p>;
    }

    return (
        <div className="transactions-list">
            {transactions.map((tx) => (
                <div key={tx.id} className="transaction-item">
                    <div className="transaction-details">
                        <div className="transaction-recipient">
                            {tx.fromIban === userIban
                                ? `To: ${tx.toFirstName} ${tx.toLastName}`
                                : `From: ${tx.fromFirstName} ${tx.fromLastName}`}
                        </div>
                        <div className="transaction-iban">
                            {formatIban(tx.fromIban === userIban ? tx.toIban : tx.fromIban)}
                        </div>
                        <div className="transaction-date">{formatDate(tx.transactionDate)}</div>
                        {tx.description && (
                            <div style={{ fontSize: '13px', color: '#888', marginTop: '5px' }}>
                                {tx.description}
                            </div>
                        )}
                    </div>
                    <div className={`transaction-amount ${tx.fromIban === userIban ? 'negative' : 'positive'}`}>
                        {tx.fromIban === userIban ? '-' : '+'}€{tx.amount.toFixed(2)}
                    </div>
                </div>
            ))}
        </div>
    );
}

export default TransactionsList;