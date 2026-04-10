import { useState } from "react";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import "./myfinance-kc.css";

export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const { kcContext } = props;
    const { url, message, realm, login } = kcContext;

    const [isSubmitting, setIsSubmitting] = useState(false);

    return (
        <div className="kc-login-card">
            
            {/* Logo do MyFinance */}
            <div className="kc-logo">
                <i className="fa-solid fa-chart-pie"></i>
                MyFinance
            </div>

            {/* Mensagem de Erro do Keycloak (se a senha estiver errada, etc) */}
            {message !== undefined && (message.type === "error" || message.type === "warning") && (
                <div className="kc-alert-error">
                    <i className="fa-solid fa-triangle-exclamation" style={{ marginRight: '8px' }}></i>
                    <span dangerouslySetInnerHTML={{ __html: message.summary }} />
                </div>
            )}

            {/* Formulário de Login */}
            <form 
                id="kc-form-login" 
                onSubmit={() => setIsSubmitting(true)} 
                action={url.loginAction} 
                method="post"
            >
                <div className="form-row">
                    <label htmlFor="username">Email ou Usuário</label>
                    <input
                        tabIndex={1}
                        id="username"
                        name="username"
                        defaultValue={login.username ?? ""}
                        type="text"
                        autoFocus
                        autoComplete="off"
                        required
                        placeholder="exemplo@email.com"
                    />
                </div>

                <div className="form-row">
                    <label htmlFor="password">Senha</label>
                    <input
                        tabIndex={2}
                        id="password"
                        name="password"
                        type="password"
                        autoComplete="off"
                        required
                        placeholder="••••••••"
                    />
                </div>

                {/* Opções extras (Lembrar de mim e Esqueci a senha) */}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', fontSize: '0.85rem' }}>
                    {realm.rememberMe && (
                        <label style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '5px' }}>
                            <input
                                tabIndex={3}
                                id="rememberMe"
                                name="rememberMe"
                                type="checkbox"
                                defaultChecked={!!login.rememberMe}
                            />
                            Lembrar de mim
                        </label>
                    )}
                    
                    {realm.resetPasswordAllowed && (
                        <a tabIndex={5} href={url.loginResetCredentialsUrl} style={{ color: 'var(--gold)', textDecoration: 'none' }}>
                            Esqueceu a senha?
                        </a>
                    )}
                </div>

                {/* Botão de Submit */}
                <button
                    tabIndex={4}
                    className="btn-primary"
                    name="login"
                    id="kc-login"
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? (
                        <span className="spinner" style={{ borderTopColor: '#000' }}></span>
                    ) : (
                        <>
                            <i className="fa-solid fa-right-to-bracket"></i>
                            Acessar Plataforma
                        </>
                    )}
                </button>

                {/* Botão de Registro corrigido para aparecer mesmo com 'Email as username' ativado */}
                {realm.password && realm.registrationAllowed && (
                    <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                        <span>Ainda não tem conta? </span>
                        <a tabIndex={6} href={url.registrationUrl} style={{ color: 'var(--gold)', textDecoration: 'none', fontWeight: 'bold' }}>
                            Cadastre-se
                        </a>
                    </div>
                )}
            </form>
        </div>
    );
}