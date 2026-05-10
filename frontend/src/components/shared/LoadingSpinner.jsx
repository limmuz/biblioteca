import React from 'react';
import styles from './LoadingSpinner.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';

export default function LoadingSpinner({ message = 'Carregando...' }) {
  return (
    <div className={styles.overlay}>
      <div className={styles.spinnerWrap}>
        <div className={styles.ring} />
        <div className={styles.iconCenter}>
          <LogoIcon className={styles.icon} />
        </div>
      </div>
      {message && <p className={styles.message}>{message}</p>}
    </div>
  );
}
