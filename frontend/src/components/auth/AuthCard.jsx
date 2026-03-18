import styles from './AuthCard.module.css';
import LogoCardIcon from '../../assets/icon-logo-card.svg?react';

export default function AuthCard({ title, children }) {
  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.logoRow}>
          <LogoCardIcon className={styles.logoIcon} aria-hidden="true" />
          <span className={styles.brandName}>Lybre</span>
        </div>
        <p className={styles.tagline}>Histórias sem limites, onde a leitura é livre!</p>
        <h1 className={styles.title}>{title}</h1>
        <hr className={styles.divider} />
        {children}
      </div>
    </div>
  );
}
